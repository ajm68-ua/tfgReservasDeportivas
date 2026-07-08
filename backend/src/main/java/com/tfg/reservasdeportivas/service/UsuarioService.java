package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.UsuarioDTO;
import com.tfg.reservasdeportivas.model.CentroDeportivo;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import com.tfg.reservasdeportivas.repository.CentroDeportivoRepository;
import com.tfg.reservasdeportivas.exception.EmailAlreadyExistsException;
import com.tfg.reservasdeportivas.exception.InvalidCredentialsException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CentroDeportivoRepository centroDeportivoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioDTO registrar(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con ese correo electrónico.");
        }

        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setRol(RolUsuario.DEPORTISTA);
        
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        Usuario guardado = usuarioRepository.save(usuario);
        UsuarioDTO mappedUser = modelMapper.map(guardado, UsuarioDTO.class);
        mappedUser.setPassword(null);
        return mappedUser;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuario -> modelMapper.map(usuario, UsuarioDTO.class));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO login(UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new InvalidCredentialsException("Correo o contraseña incorrectos");
        }

        UsuarioDTO mappedUser = modelMapper.map(usuario, UsuarioDTO.class);
        mappedUser.setPassword(null);
        return mappedUser;
    }

    @Transactional
    public UsuarioDTO actualizarPerfil(Integer id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());
        usuario.setCiudad(dto.getCiudad());
        usuario.setNivel(dto.getNivel());
        usuario.setDescripcion(dto.getDescripcion());
        if (dto.getFoto() != null) {
            usuario.setFoto(dto.getFoto());
        }
        if (dto.getNotificacionesPartidas() != null) {
            usuario.setNotificacionesPartidas(dto.getNotificacionesPartidas());
        }
        if (dto.getNotificacionesChat() != null) {
            usuario.setNotificacionesChat(dto.getNotificacionesChat());
        }

        Usuario guardado = usuarioRepository.save(usuario);
        UsuarioDTO mappedUser = modelMapper.map(guardado, UsuarioDTO.class);
        mappedUser.setPassword(null);
        return mappedUser;
    }

    @Transactional
    public void cambiarPassword(Integer id, Map<String, String> passwords) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String contrasenaActual = passwords.get("contrasenaActual");
        String nuevaContrasena = passwords.get("nuevaContrasena");

        if (!passwordEncoder.matches(contrasenaActual, usuario.getPassword())) {
            throw new InvalidCredentialsException("La contraseña actual no es correcta.");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAdministradoresByCentro(Integer centroId) {
        return usuarioRepository.findByCentroId(centroId).stream()
                .filter(u -> u.getRol() == RolUsuario.ADMINISTRADOR_CENTRO)
                .map(u -> modelMapper.map(u, UsuarioDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioDTO asignarAdministrador(Integer centroId, String email) {
        String cleanEmail = email != null ? email.trim() : "";
        Usuario usuario = usuarioRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No se ha encontrado un usuario con ese correo electrónico."));

        if (usuario.getRol() == RolUsuario.ADMINISTRADOR_CENTRO && usuario.getCentro() != null && !usuario.getCentro().getId().equals(centroId)) {
            throw new IllegalStateException("Este usuario ya es administrador de otro centro deportivo.");
        }
        
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR_CENTRO && usuario.getCentro() != null && usuario.getCentro().getId().equals(centroId)) {
            throw new IllegalStateException("Este usuario ya es administrador de tu centro.");
        }

        CentroDeportivo centro = centroDeportivoRepository.findById(centroId)
                .orElseThrow(() -> new IllegalArgumentException("Centro deportivo no encontrado."));

        usuario.setRol(RolUsuario.ADMINISTRADOR_CENTRO);
        usuario.setCentro(centro);

        Usuario guardado = usuarioRepository.save(usuario);
        return modelMapper.map(guardado, UsuarioDTO.class);
    }

    @Transactional
    public void revocarAdministrador(Integer centroId, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (usuario.getCentro() == null || !usuario.getCentro().getId().equals(centroId)) {
            throw new IllegalArgumentException("Este usuario no es administrador de tu centro.");
        }

        long adminsEnCentro = usuarioRepository.findByCentroId(centroId).stream()
                .filter(u -> u.getRol() == RolUsuario.ADMINISTRADOR_CENTRO)
                .count();

        if (adminsEnCentro <= 1) {
            throw new IllegalStateException("No puedes revocar los permisos al único administrador del centro.");
        }

        usuario.setRol(RolUsuario.DEPORTISTA);
        usuario.setCentro(null);
        usuarioRepository.save(usuario);
    }

}
