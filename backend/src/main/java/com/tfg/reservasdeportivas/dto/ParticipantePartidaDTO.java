package com.tfg.reservasdeportivas.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ParticipantePartidaDTO implements Serializable {

    private Integer id;
    private Integer reservaId;
    private Integer usuarioId;
    private String nombreUsuario;
    private LocalDateTime fechaInscripcion;

    public ParticipantePartidaDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getReservaId() { return reservaId; }
    public void setReservaId(Integer reservaId) { this.reservaId = reservaId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDateTime fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantePartidaDTO)) return false;
        ParticipantePartidaDTO that = (ParticipantePartidaDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}