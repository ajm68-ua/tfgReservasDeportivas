package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.CentroDeportivoDTO;
import com.tfg.reservasdeportivas.dto.RegistroCentroRequestDTO;
import com.tfg.reservasdeportivas.model.CentroDeportivo;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;
import com.tfg.reservasdeportivas.repository.CentroDeportivoRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import com.tfg.reservasdeportivas.exception.CentroAlreadyExistsException;
import com.tfg.reservasdeportivas.exception.EmailAlreadyExistsException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CentroDeportivoService {

    @Autowired
    private CentroDeportivoRepository centroDeportivoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<CentroDeportivoDTO> findAll() {
        return centroDeportivoRepository.findAll()
                .stream()
                .map(centro -> modelMapper.map(centro, CentroDeportivoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CentroDeportivoDTO> findById(Integer id) {
        return centroDeportivoRepository.findById(id)
                .map(centro -> modelMapper.map(centro, CentroDeportivoDTO.class));
    }

    @Transactional
    public RegistroCentroRequestDTO registrar(RegistroCentroRequestDTO request) {
        CentroDeportivoDTO centroDto = request.getCentro();
        
        if (centroDeportivoRepository.existsByNombreAndCiudad(centroDto.getNombre(), centroDto.getCiudad())) {
            throw new CentroAlreadyExistsException("Ya existe un centro deportivo con el nombre '" + centroDto.getNombre() + "' en la ciudad de '" + centroDto.getCiudad() + "'.");
        }

        if (usuarioRepository.existsByEmail(request.getAdmin().getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con ese correo electrónico.");
        }

        CentroDeportivo centro = modelMapper.map(centroDto, CentroDeportivo.class);
        CentroDeportivo centroGuardado = centroDeportivoRepository.save(centro);

        Usuario admin = modelMapper.map(request.getAdmin(), Usuario.class);
        admin.setCentro(centroGuardado);
        admin.setRol(RolUsuario.ADMINISTRADOR_CENTRO);
        admin.setPassword(passwordEncoder.encode(request.getAdmin().getPassword()));
        
        usuarioRepository.save(admin);

        request.setCentro(modelMapper.map(centroGuardado, CentroDeportivoDTO.class));
        request.getAdmin().setPassword(null);
        return request;
    }

    @Transactional
    public CentroDeportivoDTO actualizarCentro(Integer id, CentroDeportivoDTO dto) {
        CentroDeportivo centro = centroDeportivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Centro deportivo no encontrado."));

        centro.setNombre(dto.getNombre());
        centro.setDireccion(dto.getDireccion());
        centro.setCiudad(dto.getCiudad());
        centro.setTelefono(dto.getTelefono());
        centro.setHorarioApertura(dto.getHorarioApertura());
        centro.setHorarioCierre(dto.getHorarioCierre());

        if (dto.getFoto() != null) {
            centro.setFoto(dto.getFoto());
        }

        CentroDeportivo guardado = centroDeportivoRepository.save(centro);
        return modelMapper.map(guardado, CentroDeportivoDTO.class);
    }

    @Transactional
    public void eliminarCentro(Integer id) {
        CentroDeportivo centro = centroDeportivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Centro deportivo no encontrado."));

        List<Usuario> administradores = usuarioRepository.findByCentroId(id);

        if (administradores.size() > 1) {
            throw new IllegalStateException("No se puede eliminar el centro porque tiene más de un administrador asignado. Primero revoca los permisos de los demás administradores.");
        }

        for (Usuario admin : administradores) {
            admin.setCentro(null);
            admin.setRol(RolUsuario.DEPORTISTA);
            usuarioRepository.save(admin);
        }

        centroDeportivoRepository.delete(centro);
    }
}
