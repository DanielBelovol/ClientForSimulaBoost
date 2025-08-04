package com.example.client.service;

import jakarta.websocket.Session;
import org.springframework.stereotype.Service;

@Service
public class SenderMessagesToServer {
    public void sendMessageToServer(String message, Session session){
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("❌ Соединение не открыто");
        }
    }
}
