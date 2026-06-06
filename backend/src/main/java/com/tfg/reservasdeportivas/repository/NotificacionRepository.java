package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
}
