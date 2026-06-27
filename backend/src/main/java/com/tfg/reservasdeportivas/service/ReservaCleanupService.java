package com.tfg.reservasdeportivas.service;

import com.tfg.reservasdeportivas.model.Reserva;
import com.tfg.reservasdeportivas.model.Usuario;
import com.tfg.reservasdeportivas.model.enums.EstadoPago;
import com.tfg.reservasdeportivas.repository.ReservaRepository;
import com.tfg.reservasdeportivas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaCleanupService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelarReservasAbiertasIncompletas() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Reserva> parciales = reservaRepository.findByEstadoPago(EstadoPago.PAGO_PARCIAL);

        for (Reserva r : parciales) {
            if (r.getFecha().isBefore(hoy) || (r.getFecha().isEqual(hoy) && r.getHoraInicio().isBefore(ahora))) {
                r.setEstadoPago(EstadoPago.CANCELADO);
                
                Integer capacidad = r.getPista().getCapacidadMaxima();
                if (capacidad == null || capacidad == 0) capacidad = 4;
                
                BigDecimal cuota = r.getPrecioTotal().divide(BigDecimal.valueOf(capacidad), 2, java.math.RoundingMode.HALF_UP);

                for (Usuario participante : r.getParticipantes()) {
                    participante.setSaldo(participante.getSaldo().add(cuota));
                    usuarioRepository.save(participante);
                }

                r.getParticipantes().clear();
                reservaRepository.save(r);
            }
        }
    }
}
