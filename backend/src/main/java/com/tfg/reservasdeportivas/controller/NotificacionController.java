package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.NotificacionDTO;
import com.tfg.reservasdeportivas.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionDTO>> obtenerNotificaciones(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerNotificacionesPorUsuario(usuarioId));
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<Integer> contarNoLeidas(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(notificacionService.contarNoLeidas(usuarioId));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Integer id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable Integer usuarioId) {
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarNotificacion(@PathVariable Integer id) {
        notificacionService.borrarNotificacion(id);
        return ResponseEntity.ok().build();
    }
}
