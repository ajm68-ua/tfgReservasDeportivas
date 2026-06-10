package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.UsuarioDTO;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public UsuarioDTO registrar(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico.");
        }

        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setRol(RolUsuario.DEPORTISTA);

        Usuario guardado = usuarioRepository.save(usuario);
        return modelMapper.map(guardado, UsuarioDTO.class);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class));
    }
}
