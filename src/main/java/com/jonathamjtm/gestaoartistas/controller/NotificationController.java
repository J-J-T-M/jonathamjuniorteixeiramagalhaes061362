package com.jonathamjtm.gestaoartistas.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/test-connection")
    @SendTo("/topic/notifications")
    public String testConnection(String message) {
        System.out.println("Mensagem recebida via WebSocket: " + message);
        return "Conexão WebSocket estabelecida com sucesso! Você disse: " + message;
    }
}