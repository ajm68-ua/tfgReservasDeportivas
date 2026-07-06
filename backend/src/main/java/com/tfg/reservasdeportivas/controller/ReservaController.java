package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.ReservaDTO;
import com.tfg.reservasdeportivas.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/pista/{pistaId}")
    public ResponseEntity<List<ReservaDTO>> obtenerReservasPorPistaYFecha(
            @PathVariable Integer pistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(reservaService.obtenerReservasPorPistaYFecha(pistaId, fecha));
    }

    @GetMapping("/centro/{centroId}")
    public ResponseEntity<List<ReservaDTO>> obtenerReservasPorCentroYFecha(
            @PathVariable Integer centroId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        return ResponseEntity.ok(reservaService.obtenerReservasPorCentroYFecha(centroId, fecha));
    }

    @GetMapping("/abiertas")
    public ResponseEntity<List<ReservaDTO>> obtenerPartidasAbiertas() {
        return ResponseEntity.ok(reservaService.obtenerPartidasAbiertas());
    }

    @PostMapping
    public ResponseEntity<ReservaDTO> crearReserva(@RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(reservaService.crearReserva(dto));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaDTO>> obtenerReservasPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(reservaService.obtenerReservasPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> obtenerReservaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(reservaService.obtenerReservaPorId(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarReserva(@PathVariable Integer id) {
        reservaService.cancelarReserva(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Void> pagarReserva(@PathVariable Integer id) {
        reservaService.pagarReserva(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unirse")
    public ResponseEntity<?> unirsePartidaAbierta(@PathVariable Integer id, @RequestParam Integer usuarioId) {
        reservaService.unirsePartidaAbierta(id, usuarioId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/abandonar")
    public ResponseEntity<?> abandonarPartidaAbierta(@PathVariable Integer id, @RequestParam Integer usuarioId) {
        reservaService.abandonarPartidaAbierta(id, usuarioId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaDTO> modificarReserva(@PathVariable Integer id, @RequestBody ReservaDTO dto) {
        return ResponseEntity.ok(reservaService.modificarReserva(id, dto));
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivarReserva(@PathVariable Integer id) {
        reservaService.reactivarReserva(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Integer id) {
        reservaService.eliminarReserva(id);
        return ResponseEntity.ok().build();
    }
}
