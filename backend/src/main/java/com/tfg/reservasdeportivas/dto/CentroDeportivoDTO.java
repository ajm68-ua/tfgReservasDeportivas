package com.tfg.reservasdeportivas.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CentroDeportivoDTO implements Serializable {

    private Integer id;
    
    @NotBlank(message = "El nombre del centro es obligatorio")
    private String nombre;
    
    private String foto;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    
    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;
    
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;
    
    @NotNull(message = "El horario de apertura es obligatorio")
    private LocalTime horarioApertura;
    
    @NotNull(message = "El horario de cierre es obligatorio")
    private LocalTime horarioCierre;

    public CentroDeportivoDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalTime getHorarioApertura() { return horarioApertura; }
    public void setHorarioApertura(LocalTime horarioApertura) { this.horarioApertura = horarioApertura; }

    public LocalTime getHorarioCierre() { return horarioCierre; }
    public void setHorarioCierre(LocalTime horarioCierre) { this.horarioCierre = horarioCierre; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CentroDeportivoDTO)) return false;
        CentroDeportivoDTO that = (CentroDeportivoDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
