package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.PistaDTO;
import com.tfg.reservasdeportivas.service.PistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pistas")
public class PistaController {

    @Autowired
    private PistaService pistaService;

    @GetMapping
    public ResponseEntity<List<PistaDTO>> obtenerTodasLasPistas() {
        return ResponseEntity.ok(pistaService.obtenerTodasLasPistas());
    }

    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<PistaDTO>> obtenerPistasPorCentro(@PathVariable Integer centroId) {
        return ResponseEntity.ok(pistaService.obtenerPistasDisponiblesPorCentro(centroId));
    }

    @GetMapping("/admin/centro/{centroId}")
    public ResponseEntity<List<PistaDTO>> obtenerTodasPistasPorCentroAdmin(@PathVariable Integer centroId) {
        return ResponseEntity.ok(pistaService.obtenerPistasPorCentro(centroId));
    }

    @PostMapping("/centro/{centroId}")
    public ResponseEntity<PistaDTO> crearPista(@PathVariable Integer centroId, @RequestBody PistaDTO dto) {
        try {
            return ResponseEntity.ok(pistaService.crearPista(centroId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/centro/{centroId}")
    public ResponseEntity<PistaDTO> actualizarPista(@PathVariable Integer id, @PathVariable Integer centroId, @RequestBody PistaDTO dto) {
        try {
            return ResponseEntity.ok(pistaService.actualizarPista(id, centroId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/centro/{centroId}")
    public ResponseEntity<Void> eliminarPista(@PathVariable Integer id, @PathVariable Integer centroId) {
        try {
            pistaService.eliminarPista(id, centroId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
