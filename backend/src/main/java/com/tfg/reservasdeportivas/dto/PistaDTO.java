package com.tfg.reservasdeportivas.dto;

import com.tfg.reservasdeportivas.model.enums.Deporte;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class PistaDTO implements Serializable {

    private Integer id;
    private Integer centroId;
    private String centroNombre;
    private String centroCiudad;
    private String centroFoto;
    private String nombre;
    private Deporte deporte;
    private BigDecimal precioPorHora;
    private Integer capacidadMaxima;
    private Boolean disponible;

    public PistaDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCentroId() { return centroId; }
    public void setCentroId(Integer centroId) { this.centroId = centroId; }

    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }

    public String getCentroCiudad() { return centroCiudad; }
    public void setCentroCiudad(String centroCiudad) { this.centroCiudad = centroCiudad; }

    public String getCentroFoto() { return centroFoto; }
    public void setCentroFoto(String centroFoto) { this.centroFoto = centroFoto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Deporte getDeporte() { return deporte; }
    public void setDeporte(Deporte deporte) { this.deporte = deporte; }

    public BigDecimal getPrecioPorHora() { return precioPorHora; }
    public void setPrecioPorHora(BigDecimal precioPorHora) { this.precioPorHora = precioPorHora; }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PistaDTO)) return false;
        PistaDTO that = (PistaDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}