package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.CentroDeportivoDTO;
import com.tfg.reservasdeportivas.service.CentroDeportivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centros")
public class CentroDeportivoController {

    @Autowired
    private CentroDeportivoService centroDeportivoService;

    @GetMapping
    public ResponseEntity<List<CentroDeportivoDTO>> getAllCentros() {
        return ResponseEntity.ok(centroDeportivoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDeportivoDTO> getCentroById(@PathVariable Integer id) {
        return centroDeportivoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
