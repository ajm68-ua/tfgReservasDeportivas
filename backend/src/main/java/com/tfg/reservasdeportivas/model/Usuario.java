package com.tfg.reservasdeportivas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfg.reservasdeportivas.model.enums.Nivel;
import com.tfg.reservasdeportivas.model.enums.RolUsuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "centro_id")
    private CentroDeportivo centro;

    private String nombre;
    private String apellidos;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String telefono;
    private String ciudad;

    @Enumerated(EnumType.STRING)
    private Nivel nivel;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "valoracion_media", precision = 3, scale = 2)
    private BigDecimal valoracionMedia;

    @Column(precision = 10, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "notificaciones_partidas")
    private Boolean notificacionesPartidas = true;

    @Column(name = "notificaciones_chat")
    private Boolean notificacionesChat = true;

    public Usuario() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CentroDeportivo getCentro() {
        return centro;
    }

    public void setCentro(CentroDeportivo centro) {
        this.centro = centro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public BigDecimal getValoracionMedia() {
        return valoracionMedia;
    }

    public void setValoracionMedia(BigDecimal valoracionMedia) {
        this.valoracionMedia = valoracionMedia;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getNotificacionesPartidas() {
        return notificacionesPartidas;
    }

    public void setNotificacionesPartidas(Boolean notificacionesPartidas) {
        this.notificacionesPartidas = notificacionesPartidas;
    }

    public Boolean getNotificacionesChat() {
        return notificacionesChat;
    }

    public void setNotificacionesChat(Boolean notificacionesChat) {
        this.notificacionesChat = notificacionesChat;
    }
}
