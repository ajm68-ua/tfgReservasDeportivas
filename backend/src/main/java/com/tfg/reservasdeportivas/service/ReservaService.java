package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.ReservaDTO;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PistaRepository pistaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;



    public List<ReservaDTO> obtenerReservasPorPistaYFecha(Integer pistaId, LocalDate fecha) {
        return reservaRepository.findByPistaIdAndFecha(pistaId, fecha).stream()
                .map(reserva -> {
                    ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
                    if (reserva.getParticipantes() != null) {
                        dto.setParticipantesIds(reserva.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
                    }
                    if (reserva.getPista() != null) {
                        dto.setCapacidadMaxima(reserva.getPista().getCapacidadMaxima());
                    }
                    return dto;
                })
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

        java.time.LocalTime apertura = pista.getCentro().getHorarioApertura();
        java.time.LocalTime cierre = pista.getCentro().getHorarioCierre();
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
        reserva.setEstadoPago(EstadoPago.PENDIENTE);
        reserva.setPrecioTotal(calcularPrecioTotal(dto.getHoraInicio(), dto.getHoraFin(), pista.getPrecioPorHora()));

        Reserva guardada = reservaRepository.save(reserva);
        ReservaDTO result = modelMapper.map(guardada, ReservaDTO.class);
        if (guardada.getParticipantes() != null) {
            result.setParticipantesIds(guardada.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
        }
        if (guardada.getPista() != null) {
            result.setCapacidadMaxima(guardada.getPista().getCapacidadMaxima());
        }
        return result;
    }

    public List<ReservaDTO> obtenerReservasPorUsuario(Integer usuarioId) {
        return reservaRepository.findByOrganizadorIdOrderByFechaDescHoraInicioDesc(usuarioId).stream()
                .map(reserva -> {
                    ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
                    if (reserva.getParticipantes() != null) {
                        dto.setParticipantesIds(reserva.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
                    }
                    if (reserva.getPista() != null) {
                        dto.setCapacidadMaxima(reserva.getPista().getCapacidadMaxima());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public ReservaDTO obtenerReservaPorId(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
        if (reserva.getParticipantes() != null) {
            dto.setParticipantesIds(reserva.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
        }
        if (reserva.getPista() != null) {
            dto.setCapacidadMaxima(reserva.getPista().getCapacidadMaxima());
        }
        return dto;
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
    }

    public void eliminarReserva(Integer id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        boolean haPasado = reserva.getFecha().isBefore(java.time.LocalDate.now()) || 
           (reserva.getFecha().isEqual(java.time.LocalDate.now()) && reserva.getHoraInicio().isBefore(java.time.LocalTime.now()));
           
        if (reserva.getEstadoPago() != EstadoPago.CANCELADO && !haPasado) {
            throw new IllegalArgumentException("Solo se pueden eliminar reservas que están canceladas o que ya han pasado");
        }
        
        reservaRepository.delete(reserva);
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
        ReservaDTO result = modelMapper.map(guardada, ReservaDTO.class);
        if (guardada.getParticipantes() != null) {
            result.setParticipantesIds(guardada.getParticipantes().stream().map(Usuario::getId).collect(Collectors.toList()));
        }
        if (guardada.getPista() != null) {
            result.setCapacidadMaxima(guardada.getPista().getCapacidadMaxima());
        }
        return result;
    }
}
