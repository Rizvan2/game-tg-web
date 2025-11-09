package org.example.gametgweb.gameplay.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для перенаправления корневого запроса на главную страницу фронтенда.
 * <p>
 * Используется для корректной работы при прямом обращении к корню сайта —
 * перенаправляет пользователя на статический файл <b>index.html</b>,
 * который обычно является точкой входа SPA (Single Page Application) или веб-интерфейса игры.
 */
@Controller
public class FrontController {
    /**
     * Обрабатывает GET-запрос к корню приложения и выполняет редирект на статическую главную страницу.
     *
     * @return строка с инструкцией Spring MVC на выполнение редиректа к {@code /index.html}
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/index.html"; // отдаёт статический файл
    }
}
