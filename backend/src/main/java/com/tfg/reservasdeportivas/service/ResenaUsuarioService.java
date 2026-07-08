package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.ResenaUsuarioDTO;
import com.tfg.reservasdeportivas.repository.ResenaUsuarioRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import com.tfg.reservasdeportivas.repository.ReservaRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.ResenaUsuario;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResenaUsuarioService {
    @Autowired
    private ResenaUsuarioRepository resenaUsuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;


    public List<ResenaUsuarioDTO> obtenerResenasPorEvaluado(Integer evaluadoId) {
        return resenaUsuarioRepository.findByEvaluadoIdOrderByFechaCreacionDesc(evaluadoId).stream()
                .map(resena -> modelMapper.map(resena, ResenaUsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ResenaUsuarioDTO crearResena(ResenaUsuarioDTO dto) {
        if (dto.getEvaluadorId().equals(dto.getEvaluadoId())) {
            throw new IllegalArgumentException("No puedes dejar una reseña sobre ti mismo.");
        }

        if (!reservaRepository.hanCompartidoReserva(dto.getEvaluadorId(), dto.getEvaluadoId())) {
            throw new IllegalArgumentException("Solo puedes dejar una reseña a usuarios con los que hayas compartido una partida.");
        }

        Usuario evaluador = usuarioRepository.findById(dto.getEvaluadorId())
                .orElseThrow(() -> new IllegalArgumentException("Evaluador no encontrado"));
        Usuario evaluado = usuarioRepository.findById(dto.getEvaluadoId())
                .orElseThrow(() -> new IllegalArgumentException("Evaluado no encontrado"));

        ResenaUsuario resena = new ResenaUsuario();
        resena.setEvaluador(evaluador);
        resena.setEvaluado(evaluado);
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario());

        if (dto.getReservaId() != null) {
            Reserva reserva = reservaRepository.findById(dto.getReservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            resena.setReserva(reserva);
        }

        ResenaUsuario guardada = resenaUsuarioRepository.save(resena);

        return modelMapper.map(guardada, ResenaUsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public boolean hanCompartidoReserva(Integer usuario1Id, Integer usuario2Id) {
        return reservaRepository.hanCompartidoReserva(usuario1Id, usuario2Id);
    }

    @Transactional
    public ResenaUsuarioDTO modificarResena(Integer id, ResenaUsuarioDTO dto) {
        ResenaUsuario resena = resenaUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));

        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario());

        ResenaUsuario guardada = resenaUsuarioRepository.save(resena);

        return modelMapper.map(guardada, ResenaUsuarioDTO.class);
    }

    @Transactional
    public void eliminarResena(Integer id) {
        ResenaUsuario resena = resenaUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));

        resenaUsuarioRepository.delete(resena);


    }
}
