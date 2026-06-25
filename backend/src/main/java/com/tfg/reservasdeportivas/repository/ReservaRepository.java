package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Reserva;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByPistaIdAndFecha(Integer pistaId, LocalDate fecha);
    List<Reserva> findByOrganizadorIdOrderByFechaDescHoraInicioDesc(Integer organizadorId);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.pista.id = :pistaId AND r.fecha = :fecha AND r.horaInicio < :horaFin AND r.horaFin > :horaInicio AND r.estadoPago != 'CANCELADO'")
    boolean existsOverlappingReservation(@Param("pistaId") Integer pistaId, @Param("fecha") LocalDate fecha, @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);
}
