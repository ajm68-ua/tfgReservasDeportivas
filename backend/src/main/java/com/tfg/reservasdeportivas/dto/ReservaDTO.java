package com.tfg.reservasdeportivas.dto;

import com.tfg.reservasdeportivas.model.enums.EstadoPago;
import com.tfg.reservasdeportivas.model.enums.Nivel;
import com.tfg.reservasdeportivas.model.enums.Deporte;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class ReservaDTO implements Serializable {

    private Integer id;
    private Integer pistaId;
    private String nombrePista;
    private Integer centroId;
    private String nombreCentro;
    private Integer organizadorId;
    private String nombreOrganizador;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal precioTotal;
    private EstadoPago estadoPago;
    private Nivel nivel;
    private Boolean esAbierta;
    private LocalDateTime fechaCreacion;
    private List<Integer> participantesIds = new ArrayList<>();
    private Integer capacidadMaxima;
    private Deporte deporte;
    private String centroFoto;
    private List<UsuarioDTO> jugadoresDetalle = new ArrayList<>();

    public ReservaDTO() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPistaId() { return pistaId; }
    public void setPistaId(Integer pistaId) { this.pistaId = pistaId; }

    public String getNombrePista() { return nombrePista; }
    public void setNombrePista(String nombrePista) { this.nombrePista = nombrePista; }

    public Integer getCentroId() { return centroId; }
    public void setCentroId(Integer centroId) { this.centroId = centroId; }

    public String getNombreCentro() { return nombreCentro; }
    public void setNombreCentro(String nombreCentro) { this.nombreCentro = nombreCentro; }

    public Integer getOrganizadorId() { return organizadorId; }
    public void setOrganizadorId(Integer organizadorId) { this.organizadorId = organizadorId; }

    public String getNombreOrganizador() { return nombreOrganizador; }
    public void setNombreOrganizador(String nombreOrganizador) { this.nombreOrganizador = nombreOrganizador; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public Boolean getEsAbierta() { return esAbierta; }
    public void setEsAbierta(Boolean esAbierta) { this.esAbierta = esAbierta; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<Integer> getParticipantesIds() { return participantesIds; }
    public void setParticipantesIds(List<Integer> participantesIds) { this.participantesIds = participantesIds; }

    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    public Deporte getDeporte() { return deporte; }
    public void setDeporte(Deporte deporte) { this.deporte = deporte; }

    public String getCentroFoto() { return centroFoto; }
    public void setCentroFoto(String centroFoto) { this.centroFoto = centroFoto; }

    public List<UsuarioDTO> getJugadoresDetalle() { return jugadoresDetalle; }
    public void setJugadoresDetalle(List<UsuarioDTO> jugadoresDetalle) { this.jugadoresDetalle = jugadoresDetalle; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservaDTO)) return false;
        ReservaDTO that = (ReservaDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}