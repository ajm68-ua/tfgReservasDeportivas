package com.tfg.reservasdeportivas.controller;

import com.tfg.reservasdeportivas.dto.MensajeChatDTO;
import com.tfg.reservasdeportivas.service.MensajeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MensajeChatController {

    @Autowired
    private MensajeChatService mensajeChatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MensajeChatDTO chatMessage) {
        MensajeChatDTO savedMessage = mensajeChatService.guardarMensaje(chatMessage);
        messagingTemplate.convertAndSend("/topic/chat/" + chatMessage.getReservaId(), savedMessage);
    }

    @GetMapping("/api/chat/reserva/{reservaId}")
    public ResponseEntity<List<MensajeChatDTO>> getHistorialChat(@PathVariable Integer reservaId, @RequestParam Integer usuarioId) {
        return ResponseEntity.ok(mensajeChatService.obtenerMensajesPorReserva(reservaId, usuarioId));
    }
}
