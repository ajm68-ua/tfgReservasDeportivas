package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.MensajeChatDTO;
import com.tfg.reservasdeportivas.model.MensajeChat;
import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.repository.MensajeChatRepository;
import com.tfg.reservasdeportivas.repository.ReservaRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensajeChatService {

    @Autowired
    private MensajeChatRepository mensajeChatRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private ModelMapper modelMapper;

    public List<MensajeChatDTO> obtenerMensajesPorReserva(Integer reservaId, Integer usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        boolean esParticipante = reserva.getParticipantes().stream().anyMatch(u -> u.getId().equals(usuarioId));
        boolean esOrganizador = reserva.getOrganizador().getId().equals(usuarioId);

        if (!esParticipante && !esOrganizador) {
            throw new IllegalArgumentException("No tienes permiso para ver este chat");
        }

        return mensajeChatRepository.findByReservaIdOrderByFechaEnvioAsc(reservaId).stream()
                .map(mensaje -> modelMapper.map(mensaje, MensajeChatDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public MensajeChatDTO guardarMensaje(MensajeChatDTO dto) {
        Reserva reserva = reservaRepository.findById(dto.getReservaId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean esParticipante = reserva.getParticipantes().stream().anyMatch(u -> u.getId().equals(usuario.getId()));
        boolean esOrganizador = reserva.getOrganizador().getId().equals(usuario.getId());

        if (!esParticipante && !esOrganizador) {
            throw new IllegalArgumentException("No tienes permiso para enviar mensajes en este chat");
        }

        MensajeChat mensaje = new MensajeChat();
        mensaje.setReserva(reserva);
        mensaje.setUsuario(usuario);
        mensaje.setMensaje(dto.getMensaje());

        MensajeChat guardado = mensajeChatRepository.save(mensaje);
        
        notificacionService.enviarNotificacionChat(reserva, usuario, dto.getMensaje());

        return modelMapper.map(guardado, MensajeChatDTO.class);
    }

}
