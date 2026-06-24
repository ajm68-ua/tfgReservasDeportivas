package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.dto.ReservaDTO;
import com.tfg.reservasdeportivas.model.Pista;
import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.EstadoPago;
import com.tfg.reservasdeportivas.repository.PistaRepository;
import com.tfg.reservasdeportivas.repository.ReservaRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PistaRepository pistaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<ReservaDTO> obtenerReservasPorPistaYFecha(Integer pistaId, LocalDate fecha) {
        return reservaRepository.findByPistaIdAndFecha(pistaId, fecha).stream()
                .map(reserva -> modelMapper.map(reserva, ReservaDTO.class))
                .collect(Collectors.toList());
    }

    public ReservaDTO crearReserva(ReservaDTO dto) {
        Pista pista = pistaRepository.findById(dto.getPistaId())
                .orElseThrow(() -> new IllegalArgumentException("Pista no encontrada"));

        if (!pista.getDisponible()) {
            throw new IllegalArgumentException("La pista no está disponible");
        }

        Usuario organizador = usuarioRepository.findById(dto.getOrganizadorId())
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        List<Reserva> reservasDelDia = reservaRepository.findByPistaIdAndFecha(pista.getId(), dto.getFecha());
        for (Reserva r : reservasDelDia) {
            boolean solapa = dto.getHoraInicio().isBefore(r.getHoraFin()) && dto.getHoraFin().isAfter(r.getHoraInicio());
            if (solapa) {
                throw new IllegalArgumentException("La franja horaria seleccionada ya está ocupada");
            }
        }

        Reserva reserva = modelMapper.map(dto, Reserva.class);
        reserva.setPista(pista);
        reserva.setOrganizador(organizador);
        reserva.setEstadoPago(EstadoPago.PENDIENTE);

        long minutos = Duration.between(dto.getHoraInicio(), dto.getHoraFin()).toMinutes();
        double horas = minutos / 60.0;
        BigDecimal precioTotal = pista.getPrecioPorHora().multiply(BigDecimal.valueOf(horas));
        reserva.setPrecioTotal(precioTotal);

        Reserva guardada = reservaRepository.save(reserva);
        return modelMapper.map(guardada, ReservaDTO.class);
    }
}
