package com.example.client.service;

import com.example.client.mapper.Mapper;
import com.example.client.model.ProxyModel;
import com.example.client.model.RequestToVm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.*;
import java.util.List;

@Service
public class CommandHandler {
    @Autowired
    private Mapper myMapper;
    @Autowired
    private SenderMessagesToServer sender;
    private static final String[] USER_AGENTS_WIN = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:115.0) Gecko/20100101 Firefox/115.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    };

    private static final String[] USER_AGENTS_MAC = {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Safari/605.1.15",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    };

    private static final String[] USER_AGENTS_LINUX = {
            "Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    };
    private static final String[] FONTS = {
            "Arial", "Verdana", "Tahoma", "Times New Roman", "Courier New",
            "Georgia", "Trebuchet MS", "Helvetica", "Garamond", "Palatino Linotype"
    };
    private static final String[] PLATFORMS = {
            "Win32",
            "MacIntel",
            "Linux x86_64"
    };
    private static final String[] TIMEZONES = {
            "America/New_York",
            "Europe/London",
            "Asia/Tokyo",
            "Australia/Sydney",
            "UTC",
            "Europe/Berlin"
    };





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

    private WebDriver fireFoxDriver(RequestToVm request) {
        try {
            System.setProperty("webdriver.gecko.driver", "/snap/bin/geckodriver");

            ProxyModel proxyModel = request.getProxyModel();
            FirefoxProfile profile = new FirefoxProfile();

            Random random = new Random();

            // Платформа выбирается случайно
            String platform = PLATFORMS[random.nextInt(PLATFORMS.length)];

            // Таймзона берется из прокси, если есть, иначе рандомная
            String timezone;
            if (proxyModel != null && proxyModel.getTimezone() != null && !proxyModel.getTimezone().isEmpty()) {
                timezone = proxyModel.getTimezone();
            } else {
                timezone = TIMEZONES[random.nextInt(TIMEZONES.length)];
            }

            // User-agent под платформу
            String userAgent;
            if (platform.contains("Win")) {
                userAgent = USER_AGENTS_WIN[random.nextInt(USER_AGENTS_WIN.length)];
            } else if (platform.contains("Mac")) {
                userAgent = USER_AGENTS_MAC[random.nextInt(USER_AGENTS_MAC.length)];
            } else {
                userAgent = USER_AGENTS_LINUX[random.nextInt(USER_AGENTS_LINUX.length)];
            }
            profile.setPreference("general.useragent.override", userAgent);

            // Отключаем Selenium-флаги
            profile.setPreference("dom.webdriver.enabled", false);
            profile.setPreference("useAutomationExtension", false);
            profile.setPreference("media.navigator.enabled", false);
            profile.setPreference("privacy.resistFingerprinting", true);
            profile.setPreference("webgl.disabled", true);
            profile.setPreference("toolkit.telemetry.enabled", false);
            profile.setPreference("browser.safebrowsing.enabled", false);

            // Прокси настройки
            if (proxyModel != null) {
                profile.setPreference("network.proxy.type", 1);
                profile.setPreference("network.proxy.http", proxyModel.getHost());
                profile.setPreference("network.proxy.http_port", proxyModel.getPort());

                if (proxyModel.getUsername() != null && !proxyModel.getUsername().isEmpty()) {
                    String auth = proxyModel.getUsername() + ":" + proxyModel.getPassword();
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    profile.setPreference("network.proxy.http_header", "Proxy-Authorization");
                    profile.setPreference("network.proxy.http_header_value", "Basic " + encodedAuth);
                }
            } else {
                profile.setPreference("network.proxy.type", 0);
            }

            // Firefox options
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary("/usr/bin/firefox");
            options.setProfile(profile);
            options.addArguments("--headless");

            WebDriver driver = new FirefoxDriver(options);

            // Установка фиксированного размера окна — 1920x1080
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));

            // JS-спуфинг (с использованием платформы и таймзоны из прокси или рандома)
            List<String> allFonts = new ArrayList<>(Arrays.asList(FONTS));
            Collections.shuffle(allFonts, random);
            List<String> selectedFonts = allFonts.subList(0, 5 + random.nextInt(5));
            String fontListJs = "[" + String.join(",", selectedFonts.stream().map(f -> "\"" + f + "\"").toList()) + "]";

            String jsTemplate = """
    try {
        Object.defineProperty(navigator, 'platform', {
            get: () => '%s',
            configurable: true
        });

        Object.defineProperty(navigator, 'language', {
            get: () => 'en-US',
            configurable: true
        });

        const originalDTF = Intl.DateTimeFormat;
        Intl.DateTimeFormat = function(locale, options) {
            const dtf = new originalDTF(locale, options);
            const originalResolved = dtf.resolvedOptions.bind(dtf);
            dtf.resolvedOptions = () => {
                const options = originalResolved();
                options.timeZone = '%s';
                return options;
            };
            return dtf;
        };

        if (WebGLRenderingContext) {
            const getParameter = WebGLRenderingContext.prototype.getParameter;
            WebGLRenderingContext.prototype.getParameter = function(parameter) {
                if (parameter === 0x9245) return 'Intel Inc.';
                if (parameter === 0x9246) return 'Intel Iris OpenGL Engine';
                if (parameter === 37445) return 'Intel Inc.';
                if (parameter === 37446) return 'Intel Iris OpenGL Engine';
                return getParameter.apply(this, [parameter]);
            };
        }

        const fontList = %s;
        if (CanvasRenderingContext2D) {
            const originalMeasureText = CanvasRenderingContext2D.prototype.measureText;
            CanvasRenderingContext2D.prototype.measureText = function(text) {
                this.font = fontList[Math.floor(Math.random() * fontList.length)] + ' 16px';
                return originalMeasureText.apply(this, [text]);
            };
        }
    } catch (e) {
        console.error('Error in spoofing script:', e);
    }
    """;

            String jsScript = String.format(jsTemplate, platform, timezone, fontListJs);


            ((JavascriptExecutor) driver).executeScript(jsScript);

            return driver;
        } catch (Exception e) {
            System.err.println("❌ Ошибка настройки браузера: " + e.getMessage());
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
