package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.ReservaDTO;
import com.tfg.reservasdeportivas.dto.UsuarioDTO;
import com.tfg.reservasdeportivas.model.Pista;
import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.EstadoPago;
import com.tfg.reservasdeportivas.repository.PistaRepository;
import com.tfg.reservasdeportivas.repository.ReservaRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PistaRepository pistaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void notificarActualizacionCentro(Integer centroId) {
        if (messagingTemplate != null && centroId != null) {
            messagingTemplate.convertAndSend("/topic/centro/" + centroId + "/reservas", "REFRESH");
        }
    }

    private ReservaDTO mapToDTO(Reserva reserva) {
        ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
        if (reserva.getParticipantes() != null) {
            dto.setParticipantesIds(reserva.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
            dto.setJugadoresDetalle(reserva.getParticipantes().stream().map(u -> modelMapper.map(u, UsuarioDTO.class)).collect(Collectors.toList()));
        }
        if (reserva.getPista() != null) {
            dto.setCapacidadMaxima(reserva.getPista().getCapacidadMaxima());
            dto.setDeporte(reserva.getPista().getDeporte());
            if (reserva.getPista().getCentro() != null) {
                dto.setCentroFoto(reserva.getPista().getCentro().getFoto());
            }
        }
        return dto;
    }

    public List<ReservaDTO> obtenerPartidasAbiertas() {
        return reservaRepository.findPartidasAbiertasFuturas().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> obtenerReservasPorPistaYFecha(Integer pistaId, LocalDate fecha) {
        return reservaRepository.findByPistaIdAndFecha(pistaId, fecha).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> obtenerReservasPorCentroYFecha(Integer centroId, LocalDate fecha) {
        return reservaRepository.findByPistaCentroIdAndFechaOrderByHoraInicioAsc(centroId, fecha).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validarHorarioReserva(LocalDate fecha, java.time.LocalTime horaInicio, java.time.LocalTime horaFin, Pista pista, Integer reservaIgnoradaId) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
        }
        if (fecha.isEqual(LocalDate.now()) && horaInicio.isBefore(java.time.LocalTime.now())) {
            throw new IllegalArgumentException("La hora de inicio ya ha pasado");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        if (horaInicio.getMinute() != 0 || horaInicio.getSecond() != 0 ||
            horaFin.getMinute() != 0 || horaFin.getSecond() != 0) {
            throw new IllegalArgumentException("Las reservas deben ser en horas en punto");
        }
        if (!pista.getDisponible()) {
            throw new IllegalArgumentException("La pista no está disponible");
        }

        LocalTime apertura = pista.getCentro().getHorarioApertura();
        LocalTime cierre = pista.getCentro().getHorarioCierre();
        if (horaInicio.isBefore(apertura) || horaFin.isAfter(cierre)) {
            throw new IllegalArgumentException("El horario seleccionado está fuera del horario de apertura del centro");
        }

        List<Reserva> reservasDelDia = reservaRepository.findByPistaIdAndFecha(pista.getId(), fecha);
        for (Reserva r : reservasDelDia) {
            if ((reservaIgnoradaId == null || !r.getId().equals(reservaIgnoradaId)) && r.getEstadoPago() != EstadoPago.CANCELADO) {
                boolean solapa = horaInicio.isBefore(r.getHoraFin()) && horaFin.isAfter(r.getHoraInicio());
                if (solapa) {
                    throw new IllegalArgumentException("La franja horaria seleccionada ya está ocupada");
                }
            }
        }
    }

    private BigDecimal calcularPrecioTotal(java.time.LocalTime horaInicio, java.time.LocalTime horaFin, BigDecimal precioPorHora) {
        long minutos = Duration.between(horaInicio, horaFin).toMinutes();
        double horas = minutos / 60.0;
        return precioPorHora.multiply(BigDecimal.valueOf(horas));
    }

    public ReservaDTO crearReserva(ReservaDTO dto) {
        Pista pista = pistaRepository.findById(dto.getPistaId())
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));

        validarHorarioReserva(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), pista, null);

        Usuario organizador = usuarioRepository.findById(dto.getOrganizadorId())
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        Reserva reserva = modelMapper.map(dto, Reserva.class);
        reserva.setPista(pista);
        reserva.setOrganizador(organizador);
        BigDecimal precioTotal = calcularPrecioTotal(dto.getHoraInicio(), dto.getHoraFin(), pista.getPrecioPorHora());
        reserva.setPrecioTotal(precioTotal);

        if (Boolean.TRUE.equals(dto.getEsAbierta())) {
            Integer capacidad = pista.getCapacidadMaxima();
            if (capacidad == null || capacidad == 0) capacidad = 4;
            
            BigDecimal cuota = precioTotal.divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);
            
            if (organizador.getSaldo().compareTo(cuota) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente para pagar tu parte al organizar la partida abierta.");
            }
            organizador.setSaldo(organizador.getSaldo().subtract(cuota));
            usuarioRepository.save(organizador);
            
            reserva.getParticipantes().add(organizador);
            reserva.setEstadoPago(EstadoPago.PAGO_PARCIAL);
        } else {
            reserva.setEstadoPago(EstadoPago.PENDIENTE);
        }

        Reserva guardada = reservaRepository.save(reserva);

        if (Boolean.TRUE.equals(dto.getEsAbierta())) {
            notificacionService.notificarNuevaPartida(guardada);
        }
        
        notificarActualizacionCentro(guardada.getPista().getCentro().getId());

        return mapToDTO(guardada);
    }

    public List<ReservaDTO> obtenerReservasPorUsuario(Integer usuarioId) {
        return reservaRepository.findByOrganizadorIdOrderByFechaDescHoraInicioDesc(usuarioId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReservaDTO obtenerReservaPorId(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        return mapToDTO(reserva);
    }

    public void cancelarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (reserva.getFecha().isBefore(LocalDate.now()) || 
           (reserva.getFecha().isEqual(LocalDate.now()) && reserva.getHoraInicio().isBefore(java.time.LocalTime.now()))) {
            throw new IllegalArgumentException("No se puede cancelar una reserva pasada o ya comenzada");
        }

        if (reserva.getEstadoPago() == EstadoPago.CANCELADO) {
            throw new IllegalArgumentException("La reserva ya está cancelada");
        }

        if (reserva.getEstadoPago() == EstadoPago.PAGO_PARCIAL || reserva.getEstadoPago() == EstadoPago.PAGADO) {
            if (Boolean.TRUE.equals(reserva.getEsAbierta())) {
                Integer capacidad = reserva.getPista().getCapacidadMaxima();
                if (capacidad == null || capacidad == 0) capacidad = 4;
                BigDecimal cuota = reserva.getPrecioTotal().divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);
                
                for (Usuario participante : reserva.getParticipantes()) {
                    participante.setSaldo(participante.getSaldo().add(cuota));
                    usuarioRepository.save(participante);
                }
            } else {
                Usuario organizador = reserva.getOrganizador();
                organizador.setSaldo(organizador.getSaldo().add(reserva.getPrecioTotal()));
                usuarioRepository.save(organizador);
            }
        }
        
        reserva.getParticipantes().clear();
        reserva.setEstadoPago(EstadoPago.CANCELADO);
        reservaRepository.save(reserva);
        notificarActualizacionCentro(reserva.getPista().getCentro().getId());
    }

    public void reactivarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (reserva.getEstadoPago() != EstadoPago.CANCELADO) {
            throw new IllegalArgumentException("La reserva no está cancelada");
        }
        
        validarHorarioReserva(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin(), reserva.getPista(), reserva.getId());

        reserva.setEstadoPago(EstadoPago.PENDIENTE);
        reservaRepository.save(reserva);
        notificarActualizacionCentro(reserva.getPista().getCentro().getId());
    }

    public void eliminarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        boolean haPasado = reserva.getFecha().isBefore(java.time.LocalDate.now()) || 
           (reserva.getFecha().isEqual(java.time.LocalDate.now()) && reserva.getHoraInicio().isBefore(java.time.LocalTime.now()));
           
        if (reserva.getEstadoPago() != EstadoPago.CANCELADO && !haPasado) {
            throw new IllegalArgumentException("Solo se pueden eliminar reservas que están canceladas o que ya han pasado");
        }
        
        Integer centroId = reserva.getPista().getCentro().getId();
        reservaRepository.delete(reserva);
        notificarActualizacionCentro(centroId);
    }

    public void pagarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (reserva.getEstadoPago() == EstadoPago.CANCELADO) {
            throw new IllegalArgumentException("No se puede pagar una reserva cancelada");
        }
        
        Usuario organizador = reserva.getOrganizador();
        BigDecimal precioTotal = reserva.getPrecioTotal();

        if (reserva.getEsAbierta() != null && reserva.getEsAbierta()) {
            Integer capacidad = reserva.getPista().getCapacidadMaxima();
            if (capacidad == null || capacidad == 0) capacidad = 4;

            BigDecimal cuota = precioTotal.divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);
            
            if (organizador.getSaldo().compareTo(cuota) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente para pagar tu parte de la pista.");
            }

            organizador.setSaldo(organizador.getSaldo().subtract(cuota));
            usuarioRepository.save(organizador);

            reserva.getParticipantes().add(organizador);
            reserva.setEstadoPago(EstadoPago.PAGO_PARCIAL);
        } else {
            if (organizador.getSaldo().compareTo(precioTotal) < 0) {
                throw new IllegalArgumentException("Saldo insuficiente para pagar el total de la pista.");
            }

            organizador.setSaldo(organizador.getSaldo().subtract(precioTotal));
            usuarioRepository.save(organizador);

            reserva.setEstadoPago(EstadoPago.PAGADO);
        }

        reservaRepository.save(reserva);
    }

    public void unirsePartidaAbierta(Integer reservaId, Integer usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getEsAbierta() == null || !reserva.getEsAbierta()) {
            throw new IllegalArgumentException("No puedes unirte a una partida cerrada.");
        }
        if (reserva.getEstadoPago() == EstadoPago.CANCELADO || reserva.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalArgumentException("La partida ya está completa o cancelada.");
        }

        Integer capacidad = reserva.getPista().getCapacidadMaxima();
        if (capacidad == null || capacidad == 0) capacidad = 4;

        if (reserva.getParticipantes().size() >= capacidad) {
            throw new IllegalArgumentException("La partida ya está llena.");
        }

        boolean yaInscrito = reserva.getParticipantes().stream().anyMatch(u -> u.getId().equals(usuarioId));
        if (yaInscrito) {
            throw new IllegalArgumentException("Ya estás inscrito en esta partida.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        BigDecimal cuota = reserva.getPrecioTotal().divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);
        if (usuario.getSaldo().compareTo(cuota) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para unirte a la partida.");
        }

        usuario.setSaldo(usuario.getSaldo().subtract(cuota));
        usuarioRepository.save(usuario);

        reserva.getParticipantes().add(usuario);

        if (reserva.getParticipantes().size() >= capacidad) {
            reserva.setEstadoPago(EstadoPago.PAGADO);
        }

        reservaRepository.save(reserva);

        if (Boolean.TRUE.equals(reserva.getOrganizador().getNotificacionesPartidas())) {
            String titulo = "Nuevo jugador en tu partida";
            String deporteStr = reserva.getPista().getDeporte().toString().toLowerCase();
            String mensaje = usuario.getNombre() + " se ha unido a tu partida de " + deporteStr + " para el " + reserva.getFecha();
            notificacionService.crearYEnviarNotificacion(reserva.getOrganizador(), titulo, mensaje, "NUEVO_JUGADOR", reserva.getId());
        }

        notificarActualizacionCentro(reserva.getPista().getCentro().getId());
    }

    public void abandonarPartidaAbierta(Integer reservaId, Integer usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getEsAbierta() == null || !reserva.getEsAbierta()) {
            throw new IllegalArgumentException("No puedes abandonar una partida cerrada.");
        }

        if (reserva.getFecha().isBefore(java.time.LocalDate.now()) || 
           (reserva.getFecha().isEqual(java.time.LocalDate.now()) && reserva.getHoraInicio().isBefore(java.time.LocalTime.now()))) {
            throw new IllegalArgumentException("No puedes abandonar una partida pasada o ya comenzada.");
        }

        if (reserva.getEstadoPago() == EstadoPago.CANCELADO) {
            throw new IllegalArgumentException("La partida ya está cancelada.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean inscrito = reserva.getParticipantes().removeIf(u -> u.getId().equals(usuarioId));
        if (!inscrito) {
            throw new IllegalArgumentException("No estás inscrito en esta partida.");
        }

        Integer capacidad = reserva.getPista().getCapacidadMaxima();
        if (capacidad == null || capacidad == 0) capacidad = 4;
        BigDecimal cuota = reserva.getPrecioTotal().divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);

        usuario.setSaldo(usuario.getSaldo().add(cuota));
        usuarioRepository.save(usuario);

        if (reserva.getOrganizador().getId().equals(usuarioId)) {
            if (reserva.getParticipantes().isEmpty()) {
                reserva.setEstadoPago(EstadoPago.CANCELADO);
            } else {
                Usuario nuevoOrganizador = reserva.getParticipantes().get(0);
                reserva.setOrganizador(nuevoOrganizador);
                if (reserva.getEstadoPago() == EstadoPago.PAGADO) {
                    reserva.setEstadoPago(EstadoPago.PAGO_PARCIAL);
                }
            }
        } else {
            if (reserva.getEstadoPago() == EstadoPago.PAGADO) {
                reserva.setEstadoPago(EstadoPago.PAGO_PARCIAL);
            }
            if (reserva.getParticipantes().isEmpty()) {
                reserva.setEstadoPago(EstadoPago.CANCELADO);
            }
        }

        reservaRepository.save(reserva);
        notificarActualizacionCentro(reserva.getPista().getCentro().getId());
    }

    public ReservaDTO modificarReserva(Integer id, ReservaDTO dto) {
        Reserva reservaExistente = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reservaExistente.getEstadoPago() == EstadoPago.CANCELADO) {
            throw new IllegalArgumentException("No se puede modificar una reserva cancelada");
        }
        if (reservaExistente.getEstadoPago() == EstadoPago.PAGO_PARCIAL) {
            throw new IllegalArgumentException("No se puede modificar una reserva pagada parcialmente");
        }

        Pista pista = pistaRepository.findById(dto.getPistaId())
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));

        validarHorarioReserva(dto.getFecha(), dto.getHoraInicio(), dto.getHoraFin(), pista, reservaExistente.getId());

        reservaExistente.setFecha(dto.getFecha());
        reservaExistente.setHoraInicio(dto.getHoraInicio());
        reservaExistente.setHoraFin(dto.getHoraFin());
        reservaExistente.setNivel(dto.getNivel());
        reservaExistente.setEsAbierta(dto.getEsAbierta());
        reservaExistente.setPrecioTotal(calcularPrecioTotal(dto.getHoraInicio(), dto.getHoraFin(), pista.getPrecioPorHora()));

        Reserva guardada = reservaRepository.save(reservaExistente);
        notificarActualizacionCentro(guardada.getPista().getCentro().getId());
        return mapToDTO(guardada);
    }
}
