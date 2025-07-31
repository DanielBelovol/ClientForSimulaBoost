package com.example.client.service;

import com.example.client.mapper.Mapper;
import com.example.client.model.RequestFromVm;
import com.example.client.model.RequestToVm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandHandler {
    @Autowired
    private Mapper myMapper;

    public void handle(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RequestFromVm request = myMapper.requestToVmToFrom(mapper.readValue(message, RequestToVm.class));

            switch (request.getTypeOfCommand()) {
                case VIEW_ONLY -> System.out.println("👁 Просмотр: " + request.getUrl());
                case VIEW_LIKE -> System.out.println("👍 Лайк + Просмотр: " + request.getUrl());
                case VIEW_COMMENT -> System.out.println("💬 Коммент + Просмотр: " + request.getUrl());
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
}
