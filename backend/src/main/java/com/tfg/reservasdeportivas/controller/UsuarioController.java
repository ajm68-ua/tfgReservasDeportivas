package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.UsuarioDTO;
import com.tfg.reservasdeportivas.service.UsuarioService;
import com.tfg.reservasdeportivas.exception.EmailAlreadyExistsException;
import com.tfg.reservasdeportivas.exception.InvalidCredentialsException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrar(@Valid @RequestBody UsuarioDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(dto));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody UsuarioDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.login(dto));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Integer id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarPerfil(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Integer id, @RequestBody Map<String, String> passwords) {
        try {
            usuarioService.cambiarPassword(id, passwords);
            return ResponseEntity.ok().build();
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin-centro/{centroId}/administradores")
    public ResponseEntity<java.util.List<UsuarioDTO>> getAdministradores(@PathVariable Integer centroId) {
        return ResponseEntity.ok(usuarioService.findAdministradoresByCentro(centroId));
    }

    @PostMapping("/admin-centro/{centroId}/asignar")
    public ResponseEntity<?> asignarAdministrador(@PathVariable Integer centroId, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(usuarioService.asignarAdministrador(centroId, body.get("email")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/admin-centro/{centroId}/revocar/{email}")
    public ResponseEntity<?> revocarAdministrador(@PathVariable Integer centroId, @PathVariable String email) {
        try {
            usuarioService.revocarAdministrador(centroId, email);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
