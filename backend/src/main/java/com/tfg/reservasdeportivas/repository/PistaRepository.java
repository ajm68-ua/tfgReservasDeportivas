package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Pista;

public interface PistaRepository extends JpaRepository<Pista, Integer> {
}
