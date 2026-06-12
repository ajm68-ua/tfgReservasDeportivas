package com.tfg.reservasdeportivas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class RegistroCentroRequestDTO implements Serializable {

    @Valid
    @NotNull(message = "Los datos del centro son obligatorios")
    private CentroDeportivoDTO centro;

    @Valid
    @NotNull(message = "Los datos del administrador son obligatorios")
    private UsuarioDTO admin;

    public RegistroCentroRequestDTO() {
    }

    public CentroDeportivoDTO getCentro() {
        return centro;
    }

    public void setCentro(CentroDeportivoDTO centro) {
        this.centro = centro;
    }

    public UsuarioDTO getAdmin() {
        return admin;
    }

    public void setAdmin(UsuarioDTO admin) {
        this.admin = admin;
    }
}
