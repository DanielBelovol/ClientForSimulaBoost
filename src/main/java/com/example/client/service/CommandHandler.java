package com.example.client.service;

import com.example.client.mapper.Mapper;
import com.example.client.model.ProxyModel;
import com.example.client.model.RequestToVm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Map;

@Service
public class CommandHandler {
    @Autowired
    private Mapper myMapper;
    @Autowired
    private SenderMessagesToServer sender;

    public void handle(@RequestBody RequestToVm request, Session session) {
        try {
            if(request==null){
                sender.sendMessageToServer("requst is empty", session);
            }

            switch (request.getTypeOfCommand()) {
                case VIEW_ONLY -> {
                    System.out.println("👁 Просмотр: " + request.getUrl());
                    startBrowserWithProxy(request.getProxyModel());
                }
                case VIEW_LIKE -> {
                    System.out.println("👍 Лайк + Просмотр: " + request.getUrl());
                    startBrowserWithProxy(request.getProxyModel());
                }
                case VIEW_COMMENT -> {
                    System.out.println("💬 Коммент + Просмотр: " + request.getUrl());
                    startBrowserWithProxy(request.getProxyModel());
                }
                case STOP -> {
                    System.out.println("🛑 Остановка клиента");
                    System.exit(0);
                }
                default -> System.out.println("Неизвестная команда");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startBrowserWithProxy(ProxyModel proxyModel) {
        try {
            String proxyHost = proxyModel.getIp();
            int proxyPort = proxyModel.getPort();
            String proxyUser = proxyModel.getUser();
            String proxyPass = proxyModel.getPassword();

            // Формируем URL прокси с учетом логина и пароля
            String proxyUrl = "http://" + proxyHost + ":" + proxyPort;
            if (proxyUser != null && !proxyUser.isEmpty() && proxyPass != null && !proxyPass.isEmpty()) {
                proxyUrl = "http://" + proxyUser + ":" + proxyPass + "@" + proxyHost + ":" + proxyPort;
            }

            // Запускаем браузер (например, Firefox) без URL
            ProcessBuilder processBuilder = new ProcessBuilder("firefox");
            Map<String, String> env = processBuilder.environment();

            // Устанавливаем переменные окружения для прокси
            env.put("http_proxy", proxyUrl);
            env.put("https_proxy", proxyUrl);

            // Запускаем процесс
            Process process = processBuilder.start();
            process.waitFor(); // Ожидаем завершения (если нужно)
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Ошибка запуска браузера: " + e.getMessage());
        }
    }
}
