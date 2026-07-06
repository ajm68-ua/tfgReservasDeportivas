package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.Reserva;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tfg.reservasdeportivas.model.enums.EstadoPago;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByPistaIdAndFecha(Integer pistaId, LocalDate fecha);
    List<Reserva> findByPistaCentroIdAndFechaOrderByHoraInicioAsc(Integer centroId, LocalDate fecha);
    @Query("SELECT DISTINCT r FROM Reserva r LEFT JOIN r.participantes p WHERE r.organizador.id = :usuarioId OR p.id = :usuarioId ORDER BY r.fecha DESC, r.horaInicio DESC")
    List<Reserva> findByOrganizadorIdOrderByFechaDescHoraInicioDesc(@Param("usuarioId") Integer usuarioId);
    List<Reserva> findByEstadoPago(EstadoPago estadoPago);
    
    @Query("SELECT r FROM Reserva r WHERE r.esAbierta = true AND r.estadoPago != 'CANCELADO' AND (r.fecha > CURRENT_DATE OR (r.fecha = CURRENT_DATE AND r.horaInicio > CURRENT_TIME)) ORDER BY r.fecha ASC, r.horaInicio ASC")
    List<Reserva> findPartidasAbiertasFuturas();

    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.pista.id = :pistaId AND r.fecha = :fecha AND r.horaInicio < :horaFin AND r.horaFin > :horaInicio AND r.estadoPago != 'CANCELADO'")
    boolean existsOverlappingReservation(@Param("pistaId") Integer pistaId, @Param("fecha") LocalDate fecha, @Param("horaInicio") LocalTime horaInicio, @Param("horaFin") LocalTime horaFin);
}
