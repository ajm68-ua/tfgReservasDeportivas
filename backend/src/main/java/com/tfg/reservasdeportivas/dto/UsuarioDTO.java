package com.tfg.reservasdeportivas.dto;

import com.tfg.reservasdeportivas.model.enums.Nivel;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class UsuarioDTO implements Serializable {

    private Integer id;
    private Integer centroId;
    private String nombre;
    private String apellidos;
    private String foto;
    private String email;
    private String password;
    private String telefono;
    private String ciudad;
    private Nivel nivel;
    private RolUsuario rol;
    private LocalDateTime fechaRegistro;
    private BigDecimal valoracionMedia;

    public UsuarioDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCentroId() { return centroId; }
    public void setCentroId(Integer centroId) { this.centroId = centroId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public BigDecimal getValoracionMedia() { return valoracionMedia; }
    public void setValoracionMedia(BigDecimal valoracionMedia) { this.valoracionMedia = valoracionMedia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioDTO)) return false;
        UsuarioDTO that = (UsuarioDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}