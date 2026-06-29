package com.tfg.reservasdeportivas.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ResenaUsuarioDTO implements Serializable {

    private Integer id;
    private Integer reservaId;
    private Integer evaluadorId;
    private String nombreEvaluador;
    private Integer evaluadoId;
    private String nombreEvaluado;
    @Min(0)
    @Max(5)
    private Integer puntuacion;
    private String comentario;
    private LocalDateTime fechaCreacion;

    public ResenaUsuarioDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getReservaId() { return reservaId; }
    public void setReservaId(Integer reservaId) { this.reservaId = reservaId; }

    public Integer getEvaluadorId() { return evaluadorId; }
    public void setEvaluadorId(Integer evaluadorId) { this.evaluadorId = evaluadorId; }

    public String getNombreEvaluador() { return nombreEvaluador; }
    public void setNombreEvaluador(String nombreEvaluador) { this.nombreEvaluador = nombreEvaluador; }

    public Integer getEvaluadoId() { return evaluadoId; }
    public void setEvaluadoId(Integer evaluadoId) { this.evaluadoId = evaluadoId; }

    public String getNombreEvaluado() { return nombreEvaluado; }
    public void setNombreEvaluado(String nombreEvaluado) { this.nombreEvaluado = nombreEvaluado; }

    public Integer getPuntuacion() { return puntuacion; }
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResenaUsuarioDTO)) return false;
        ResenaUsuarioDTO that = (ResenaUsuarioDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}