package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.CentroDeportivo;

public interface CentroDeportivoRepository extends JpaRepository<CentroDeportivo, Integer> {
}
