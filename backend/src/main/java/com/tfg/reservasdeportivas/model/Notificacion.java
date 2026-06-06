package com.tfg.reservasdeportivas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    private Boolean leido;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    public Notificacion() {
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
