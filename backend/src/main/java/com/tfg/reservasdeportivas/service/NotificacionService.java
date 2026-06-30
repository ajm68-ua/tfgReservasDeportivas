package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.NotificacionDTO;
import com.tfg.reservasdeportivas.model.Notificacion;
import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.repository.NotificacionRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<NotificacionDTO> obtenerNotificacionesPorUsuario(Integer usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(n -> modelMapper.map(n, NotificacionDTO.class))
                .collect(Collectors.toList());
    }

    public int contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidoFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLeida(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        notificacion.setLeido(true);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarTodasComoLeidas(Integer usuarioId) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().filter(n -> !n.getLeido()).collect(Collectors.toList());
        
        for (Notificacion n : noLeidas) {
            n.setLeido(true);
        }
        notificacionRepository.saveAll(noLeidas);
    }

    @Transactional
    public void crearYEnviarNotificacion(Usuario usuario, String titulo, String mensaje, String tipo, Integer referenciaId) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setReferenciaId(referenciaId);
        notificacion.setLeido(false);

        Notificacion guardada = notificacionRepository.save(notificacion);
        NotificacionDTO dto = modelMapper.map(guardada, NotificacionDTO.class);

        messagingTemplate.convertAndSend("/topic/notificaciones/" + usuario.getId(), dto);
    }

    public void enviarNotificacionChat(Reserva reserva, Usuario remitente, String mensajeTexto) {
        String titulo = "Nuevo mensaje en " + reserva.getPista().getCentro().getNombre();
        String mensajeCorto = remitente.getNombre() + ": " + (mensajeTexto.length() > 30 ? mensajeTexto.substring(0, 30) + "..." : mensajeTexto);

        for (Usuario participante : reserva.getParticipantes()) {
            if (!participante.getId().equals(remitente.getId()) && Boolean.TRUE.equals(participante.getNotificacionesChat())) {
                crearYEnviarNotificacion(participante, titulo, mensajeCorto, "CHAT", reserva.getId());
            }
        }

        Usuario organizador = reserva.getOrganizador();
        if (!organizador.getId().equals(remitente.getId()) 
            && !reserva.getParticipantes().contains(organizador) 
            && Boolean.TRUE.equals(organizador.getNotificacionesChat())) {
            crearYEnviarNotificacion(organizador, titulo, mensajeCorto, "CHAT", reserva.getId());
        }
    }

    public void notificarNuevaPartida(Reserva reserva) {
        if (!Boolean.TRUE.equals(reserva.getEsAbierta())) return;

        String ciudadPista = reserva.getPista().getCentro().getCiudad();
        if (ciudadPista == null || ciudadPista.trim().isEmpty()) return;

        List<Usuario> usuariosEnCiudad = usuarioRepository.findByCiudadIgnoreCase(ciudadPista);

        String titulo = "Nueva partida abierta en " + ciudadPista;
        String mensaje = "Se ha creado una partida de " + reserva.getPista().getDeporte().toString().toLowerCase() + " en " + reserva.getPista().getCentro().getNombre() + " para el " + reserva.getFecha();

        for (Usuario usuario : usuariosEnCiudad) {
            if (!usuario.getId().equals(reserva.getOrganizador().getId()) && Boolean.TRUE.equals(usuario.getNotificacionesPartidas())) {
                crearYEnviarNotificacion(usuario, titulo, mensaje, "NUEVA_PARTIDA", reserva.getId());
            }
        }
    }

    @Transactional
    public void borrarNotificacion(Integer id) {
        if (notificacionRepository.existsById(id)) {
            notificacionRepository.deleteById(id);
        }
    }
}
