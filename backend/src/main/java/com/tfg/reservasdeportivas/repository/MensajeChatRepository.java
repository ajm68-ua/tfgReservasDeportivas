package com.tfg.reservasdeportivas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tfg.reservasdeportivas.model.MensajeChat;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Integer> {
}
