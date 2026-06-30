package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Notificacion;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    int countByUsuarioIdAndLeidoFalse(Integer usuarioId);
}
