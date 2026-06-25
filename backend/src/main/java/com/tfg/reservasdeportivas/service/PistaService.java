package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.PistaDTO;
import com.tfg.reservasdeportivas.repository.PistaRepository;
import com.tfg.reservasdeportivas.repository.CentroDeportivoRepository;
import com.tfg.reservasdeportivas.model.Pista;
import com.tfg.reservasdeportivas.model.CentroDeportivo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PistaService {

    @Autowired
    private PistaRepository pistaRepository;

    @Autowired
    private CentroDeportivoRepository centroDeportivoRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<PistaDTO> obtenerTodasLasPistas() {
        return pistaRepository.findByDisponibleTrue().stream()
                .map(pista -> modelMapper.map(pista, PistaDTO.class))
                .collect(Collectors.toList());
    }

    public List<PistaDTO> obtenerPistasDisponiblesPorCentro(Integer centroId) {
        return pistaRepository.findByCentroId(centroId).stream()
                .filter(Pista::getDisponible)
                .map(pista -> modelMapper.map(pista, PistaDTO.class))
                .collect(Collectors.toList());
    }

    public PistaDTO obtenerPistaPorId(Integer id) {
        Pista pista = pistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));
        return modelMapper.map(pista, PistaDTO.class);
    }

    public List<PistaDTO> obtenerPistasPorCentro(Integer centroId) {
        return pistaRepository.findByCentroId(centroId).stream()
                .map(pista -> modelMapper.map(pista, PistaDTO.class))
                .collect(Collectors.toList());
    }

    public PistaDTO crearPista(Integer centroId, PistaDTO dto) {
        CentroDeportivo centro = centroDeportivoRepository.findById(centroId)
                .orElseThrow(() -> new IllegalArgumentException("Centro deportivo no encontrado"));

        Pista pista = modelMapper.map(dto, Pista.class);
        pista.setCentro(centro);
        if (pista.getDisponible() == null) {
            pista.setDisponible(true);
        }

        Pista guardada = pistaRepository.save(pista);
        return modelMapper.map(guardada, PistaDTO.class);
    }

    public PistaDTO actualizarPista(Integer id, Integer centroId, PistaDTO dto) {
        Pista pista = pistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));

        if (!pista.getCentro().getId().equals(centroId)) {
            throw new IllegalArgumentException("No tienes permisos para modificar esta pista");
        }

        pista.setNombre(dto.getNombre());
        pista.setDeporte(dto.getDeporte());
        pista.setPrecioPorHora(dto.getPrecioPorHora());
        pista.setCapacidadMaxima(dto.getCapacidadMaxima());
        if (dto.getDisponible() != null) {
            pista.setDisponible(dto.getDisponible());
        }

        Pista actualizada = pistaRepository.save(pista);
        return modelMapper.map(actualizada, PistaDTO.class);
    }

    public void eliminarPista(Integer id, Integer centroId) {
        Pista pista = pistaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));

        if (!pista.getCentro().getId().equals(centroId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar esta pista");
        }

        pistaRepository.delete(pista);
    }
}
