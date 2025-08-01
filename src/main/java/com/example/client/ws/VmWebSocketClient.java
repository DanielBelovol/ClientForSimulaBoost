package com.example.client.ws;

import com.example.client.service.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.client.mapper.Mapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.net.URI;

@Component
@ClientEndpoint
public class VmWebSocketClient {

    private Session session;

    private final CommandHandler commandHandler;
    private final ObjectMapper objectMapper;
    private final Mapper myMapper;

    @Value("${websocket.server.uri}")
    private String serverUri;

    @Value("${vm.id}")
    private String vmId;

    public VmWebSocketClient(CommandHandler commandHandler, ObjectMapper objectMapper, Mapper myMapper) {
        this.commandHandler = commandHandler;
        this.objectMapper = objectMapper;
        this.myMapper = myMapper;
    }

    @PostConstruct
    public void connect() {
        try {
            String fullUri = serverUri + "?vmId=" + vmId;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(fullUri));
            System.out.println("Подключение к WebSocket серверу: " + fullUri);
        } catch (Exception e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("✅ WebSocket соединение установлено");
        // Например, можно отправить сообщение после подключения:
        sendMessage("Привет сервер!");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("📩 Получено сообщение: " + message);
        try {
            commandHandler.handle(message);
        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("🔌 Соединение закрыто: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("❌ Ошибка WebSocket: " + throwable.getMessage());
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("❌ Соединение не открыто");
        }
    }
}