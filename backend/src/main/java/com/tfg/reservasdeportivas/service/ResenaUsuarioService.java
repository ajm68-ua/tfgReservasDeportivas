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

    @Autowired
    private UsuarioService usuarioService;

    public List<ResenaUsuarioDTO> obtenerResenasPorEvaluado(Integer evaluadoId) {
        return resenaUsuarioRepository.findByEvaluadoIdOrderByFechaCreacionDesc(evaluadoId).stream()
                .map(resena -> modelMapper.map(resena, ResenaUsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ResenaUsuarioDTO crearResena(ResenaUsuarioDTO dto) {
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

        List<ResenaUsuario> todas = resenaUsuarioRepository.findByEvaluadoIdOrderByFechaCreacionDesc(evaluado.getId());
        usuarioService.recalcularValoracionMedia(evaluado.getId(), todas);

        return modelMapper.map(guardada, ResenaUsuarioDTO.class);
    }

    @Transactional
    public ResenaUsuarioDTO modificarResena(Integer id, ResenaUsuarioDTO dto) {
        ResenaUsuario resena = resenaUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));

        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario());

        ResenaUsuario guardada = resenaUsuarioRepository.save(resena);

        List<ResenaUsuario> todas = resenaUsuarioRepository.findByEvaluadoIdOrderByFechaCreacionDesc(resena.getEvaluado().getId());
        usuarioService.recalcularValoracionMedia(resena.getEvaluado().getId(), todas);

        return modelMapper.map(guardada, ResenaUsuarioDTO.class);
    }

    @Transactional
    public void eliminarResena(Integer id) {
        ResenaUsuario resena = resenaUsuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada"));

        Integer evaluadoId = resena.getEvaluado().getId();
        resenaUsuarioRepository.delete(resena);

        List<ResenaUsuario> todas = resenaUsuarioRepository.findByEvaluadoIdOrderByFechaCreacionDesc(evaluadoId);
        usuarioService.recalcularValoracionMedia(evaluadoId, todas);
    }
}
