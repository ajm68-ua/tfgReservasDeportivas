package com.tfg.reservasdeportivas.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centros_deportivos")
public class CentroDeportivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String foto;
    private String direccion;
    private String ciudad;
    private String telefono;

    @Column(name = "horario_apertura")
    private LocalTime horarioApertura;

    @Column(name = "horario_cierre")
    private LocalTime horarioCierre;

    @OneToMany(mappedBy = "centro")
    private List<Pista> pistas = new ArrayList<>();

    @OneToMany(mappedBy = "centro")
    private List<Usuario> usuarios = new ArrayList<>();

    public CentroDeportivo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalTime getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(LocalTime horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }

    public List<Pista> getPistas() {
        return pistas;
    }

    public void setPistas(List<Pista> pistas) {
        this.pistas = pistas;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
