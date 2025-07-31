package com.example.client.ws;

import com.example.client.service.CommandHandler;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Component

public class VmWebSocketClient extends WebSocketClient {

    private static String vmId;
    @Autowired
    private CommandHandler commandHandler;

    public VmWebSocketClient(@Value("${ws.server-uri}") String serverUri, CommandHandler commandHandler) throws Exception {
        super(new URI(serverUri));
        VmWebSocketClient.vmId = getVmId();
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void init() {
        this.connect(); // подключение при запуске Spring Boot
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✅ Connected to server");
        send(vmId); // отправляем свой VM ID на сервер
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message.toString());
        commandHandler.handle(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Connection closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("⚠️ Error:");
        ex.printStackTrace();
    }

    private static String getVmId() {
        try {
            return Files.readString(Path.of("/sys/class/dmi/id/product_uuid")).trim();
        } catch (Exception e) {
            return "unknown-vm";
        }
    }
}
