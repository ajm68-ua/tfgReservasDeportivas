package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.MensajeChat;

import java.util.List;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Integer> {
    List<MensajeChat> findByReservaIdOrderByFechaEnvioAsc(Integer reservaId);
}
