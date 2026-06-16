package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.PistaDTO;
import com.tfg.reservasdeportivas.service.PistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
