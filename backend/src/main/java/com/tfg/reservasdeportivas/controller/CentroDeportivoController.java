package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.CentroDeportivoDTO;
import com.tfg.reservasdeportivas.dto.RegistroCentroRequestDTO;
import com.tfg.reservasdeportivas.service.CentroDeportivoService;
import com.tfg.reservasdeportivas.exception.CentroAlreadyExistsException;
import com.tfg.reservasdeportivas.exception.EmailAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/registro")
    public ResponseEntity<RegistroCentroRequestDTO> registrar(@Valid @RequestBody RegistroCentroRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(centroDeportivoService.registrar(request));
        } catch (CentroAlreadyExistsException | EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDeportivoDTO> getCentroById(@PathVariable Integer id) {
        return centroDeportivoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
