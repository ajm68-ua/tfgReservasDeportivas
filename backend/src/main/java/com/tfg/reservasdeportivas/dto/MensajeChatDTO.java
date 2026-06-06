package com.tfg.reservasdeportivas.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class MensajeChatDTO implements Serializable {

    private Integer id;
    private Integer reservaId;
    private Integer usuarioId;
    private String nombreUsuario;
    private String fotoUsuario;
    private String mensaje;
    private LocalDateTime fechaEnvio;

    public MensajeChatDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getReservaId() { return reservaId; }
    public void setReservaId(Integer reservaId) { this.reservaId = reservaId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getFotoUsuario() { return fotoUsuario; }
    public void setFotoUsuario(String fotoUsuario) { this.fotoUsuario = fotoUsuario; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MensajeChatDTO)) return false;
        MensajeChatDTO that = (MensajeChatDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
