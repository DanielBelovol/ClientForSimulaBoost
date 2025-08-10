package com.example.client.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;

@Service
public class RobotMouseService {
    private final Random random = new Random();

    public void mouseToSearchMenu() {
        try {
            Robot robot = new Robot();

            // Целевая точка — рандомно в области поиска
            int targetX = 635 + random.nextInt(1100 - 635);
            int targetY = 10 + random.nextInt(40 - 10);

            // Текущая позиция курсора
            Point start = MouseInfo.getPointerInfo().getLocation();
            Point end = new Point(targetX, targetY);

            // Контрольные точки для кривой Безье (рандомно, чтобы путь не был прямой)
            Point ctrl1 = new Point(
                    (start.x + end.x) / 2 + random.nextInt(100) - 50,
                    (start.y + end.y) / 2 + random.nextInt(100) - 50
            );

            moveMouseSmooth(robot, start, ctrl1, end);

            // Клик
            robot.delay(300 + random.nextInt(300));
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void moveMouseSmooth(Robot robot, Point p0, Point p1, Point p2) {
        int steps = 100 + random.nextInt(50); // Кол-во шагов (мелких движений)

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;

            // Кривая Безье второго порядка
            int x = (int) ((1 - t) * (1 - t) * p0.x + 2 * (1 - t) * t * p1.x + t * t * p2.x);
            int y = (int) ((1 - t) * (1 - t) * p0.y + 2 * (1 - t) * t * p1.y + t * t * p2.y);

            robot.mouseMove(x, y);

            // Разное время задержки для имитации "неровной" руки
            robot.delay(5 + random.nextInt(10));
        }
    }
}
