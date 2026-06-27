package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Pista;

import java.util.List;

public interface PistaRepository extends JpaRepository<Pista, Integer> {
    List<Pista> findByCentroId(Integer centroId);
    List<Pista> findByDisponibleTrue();
}
