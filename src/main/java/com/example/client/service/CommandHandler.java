package com.example.client.service;

import com.example.client.mapper.Mapper;
import com.example.client.model.ProxyModel;
import com.example.client.model.RequestToVm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Base64;
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
                    startBrowserAndSearch(request);
                }
                case VIEW_LIKE -> {
                    System.out.println("👍 Лайк + Просмотр: " + request.getUrl());
                    startBrowserAndSearch(request);
                }
                case VIEW_COMMENT -> {
                    System.out.println("💬 Коммент + Просмотр: " + request.getUrl());
                    startBrowserAndSearch(request);
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
    public void startBrowserAndSearch(RequestToVm request) {
        WebDriver driver = fireFoxDriver(request);
        if (driver != null) {
            try {
                // Ждем, пока браузер загрузится
                Thread.sleep(3000);

                // Создаем Robot для автоматизации
                Robot robot = new Robot();

                // Нажимаем Ctrl+L для фокуса на адресную строку
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_L);
                robot.keyRelease(KeyEvent.VK_L);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.delay(500); // Задержка после фокуса

                // Вводим "youtube" в адресную строку
                String searchText = "youtube";
                for (char c : searchText.toCharArray()) {
                    typeCharacter(robot, c);
                }

                // Нажимаем Enter
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

                // Ждем загрузки страницы
                Thread.sleep(5000);

                // Закрываем браузер
                driver.quit();
            } catch (Exception e) {
                System.err.println("❌ Ошибка открытия сайта: " + e.getMessage());
                driver.quit();
            }
        }
    }

    public WebDriver fireFoxDriver(RequestToVm request){
        try {
            System.setProperty("webdriver.gecko.driver", "/snap/bin/geckodriver");

            ProxyModel proxyModel = request.getProxyModel();
            // Создаём профиль Firefox
            FirefoxProfile profile = new FirefoxProfile();

            // Настройка прокси
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyModel.getHost() + ":" + proxyModel.getPort());

            // Добавляем заголовок Proxy-Authorization
            String auth = proxyModel.getUsername() + ":" + proxyModel.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            profile.setPreference("network.proxy.http_header", "Proxy-Authorization");
            profile.setPreference("network.proxy.http_header_value", "Basic " + encodedAuth);

            // Настройки Firefox
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary("/usr/bin/firefox");
            options.setProxy(proxy);
            options.setProfile(profile);

            // Запуск браузера
            WebDriver driver = new FirefoxDriver(options);
            driver.manage().window().fullscreen(); // Открытие на весь экран
            return driver;
        } catch (Exception e) {
            System.err.println("❌ Ошибка настройки прокси: " + e.getMessage());
            return null;
        }
    }

    // Вспомогательный метод для ввода символов
    private void typeCharacter(Robot robot, char c) {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (keyCode != KeyEvent.VK_UNDEFINED) {
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
            robot.delay(100); // Задержка между нажатиями
        } else {
            System.err.println("❌ Неизвестный символ: " + c);
        }
    }
}
