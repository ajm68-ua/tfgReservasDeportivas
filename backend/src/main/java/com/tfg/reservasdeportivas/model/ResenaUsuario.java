package com.tfg.reservasdeportivas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas_usuarios")
public class ResenaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluador_id", nullable = false)
    private Usuario evaluador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluado_id", nullable = false)
    private Usuario evaluado;

    private Integer puntuacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    public ResenaUsuario() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
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

    public Usuario getEvaluador() {
        return evaluador;
    }

    public void setEvaluador(Usuario evaluador) {
        this.evaluador = evaluador;
    }

    public Usuario getEvaluado() {
        return evaluado;
    }

    public void setEvaluado(Usuario evaluado) {
        this.evaluado = evaluado;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
