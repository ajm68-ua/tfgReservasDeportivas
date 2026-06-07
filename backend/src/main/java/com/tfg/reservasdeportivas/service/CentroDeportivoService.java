package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.CentroDeportivoDTO;
import com.tfg.reservasdeportivas.repository.CentroDeportivoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CentroDeportivoService {

    @Autowired
    private CentroDeportivoRepository centroDeportivoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<CentroDeportivoDTO> findAll() {
        return centroDeportivoRepository.findAll()
                .stream()
                .map(centro -> modelMapper.map(centro, CentroDeportivoDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CentroDeportivoDTO> findById(Integer id) {
        return centroDeportivoRepository.findById(id)
                .map(centro -> modelMapper.map(centro, CentroDeportivoDTO.class));
    }
}
