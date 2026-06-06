package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
}
