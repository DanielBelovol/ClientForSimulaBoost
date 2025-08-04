package com.example.client.ws;

import com.example.client.model.ProxyModel;
import com.example.client.model.RequestToVm;
import com.example.client.service.CommandHandler;
import com.example.client.service.SenderMessagesToServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.client.mapper.Mapper;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


import java.net.URI;

@Component
@ClientEndpoint
@AllArgsConstructor
@NoArgsConstructor
public class VmWebSocketClient {

    private Session session;
    @Autowired
    private CommandHandler commandHandler;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Mapper myMapper;
    @Autowired
    private SenderMessagesToServer sender;

    @Value("${websocket.server.uri}")
    private String serverUri;

    @Value("${vm.id}")
    private String vmId;


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
        sender.sendMessageToServer("Привет сервер!",session);
    }

    @OnMessage
    public void onMessage(@RequestBody RequestToVm requestToVm, Session session) {
        System.out.println("📩 Получено сообщение: " + requestToVm.toString());
        try {
            commandHandler.handle(requestToVm, session);
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
}