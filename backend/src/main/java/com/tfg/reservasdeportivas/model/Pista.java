package com.tfg.reservasdeportivas.model;

import com.tfg.reservasdeportivas.model.enums.Deporte;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pistas")
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private CentroDeportivo centro;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private Deporte deporte;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(name = "precio_por_hora", precision = 10, scale = 2)
    private BigDecimal precioPorHora;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    private Boolean disponible;

    public Pista() {
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

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public BigDecimal getPrecioPorHora() {
        return precioPorHora;
    }

    public void setPrecioPorHora(BigDecimal precioPorHora) {
        this.precioPorHora = precioPorHora;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
}
