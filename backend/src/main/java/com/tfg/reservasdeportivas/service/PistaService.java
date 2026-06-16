package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.PistaDTO;
import com.tfg.reservasdeportivas.repository.PistaRepository;
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
    private ModelMapper modelMapper;

    public List<PistaDTO> obtenerTodasLasPistas() {
        return pistaRepository.findAll().stream()
                .map(pista -> modelMapper.map(pista, PistaDTO.class))
                .collect(Collectors.toList());
    }
}
