package com.jonathamjtm.gestaoartistas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "7. Real-time Notifications", description = "Documentação dos fluxos de mensagens via WebSocket (STOMP)")
public class NotificationController {

    @MessageMapping("/test-connection")
    @SendTo("/topic/notifications")
    public String testConnection(String message) {
        System.out.println("Mensagem recebida via WebSocket: " + message);
        return "Conexão WebSocket estabelecida com sucesso! Você disse: " + message;
    }
}