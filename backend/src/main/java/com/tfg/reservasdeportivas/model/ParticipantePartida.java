package com.tfg.reservasdeportivas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participantes_partida")
public class ParticipantePartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inscripcion", updatable = false)
    private LocalDateTime fechaInscripcion;

    public ParticipantePartida() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaInscripcion = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }
}
