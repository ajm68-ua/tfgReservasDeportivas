package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.ResenaUsuarioDTO;
import com.tfg.reservasdeportivas.service.ResenaUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/resenas/usuario")
public class ResenaUsuarioController {
    
    @Autowired
    private ResenaUsuarioService resenaUsuarioService;

    @GetMapping("/{id}")
    public ResponseEntity<List<ResenaUsuarioDTO>> obtenerResenasPorUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(resenaUsuarioService.obtenerResenasPorEvaluado(id));
    }

    @GetMapping("/pueden-resenar/{usuario1Id}/{usuario2Id}")
    public ResponseEntity<Boolean> puedenResenar(@PathVariable Integer usuario1Id, @PathVariable Integer usuario2Id) {
        return ResponseEntity.ok(resenaUsuarioService.hanCompartidoReserva(usuario1Id, usuario2Id));
    }

    @PostMapping
    public ResponseEntity<ResenaUsuarioDTO> crearResena(@Valid @RequestBody ResenaUsuarioDTO dto) {
        return ResponseEntity.ok(resenaUsuarioService.crearResena(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResenaUsuarioDTO> modificarResena(@PathVariable Integer id, @Valid @RequestBody ResenaUsuarioDTO dto) {
        return ResponseEntity.ok(resenaUsuarioService.modificarResena(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Integer id) {
        resenaUsuarioService.eliminarResena(id);
        return ResponseEntity.ok().build();
    }
}
