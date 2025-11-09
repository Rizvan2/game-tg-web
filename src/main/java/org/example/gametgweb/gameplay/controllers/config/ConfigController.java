package org.example.gametgweb.gameplay.controllers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST-контроллер для предоставления клиентской части (фронтенда)
 * конфигурационных параметров приложения.
 * <p>
 * Основное предназначение — передача клиенту базового URL сервера,
 * который используется для формирования корректных запросов из браузера
 * к backend API или WebSocket-эндпоинтам.
 */
@RestController
public class ConfigController {
    /**
     * Базовый URL сервера, задаётся в application.properties или через переменные окружения.
     */
    @Value("${game.base-url}")
    private String baseUrl;

    /**
     * Возвращает конфигурационные параметры в виде JSON-объекта.
     * <p>
     * Пример ответа:
     * <pre>
     * {
     *   "baseUrl": "http://localhost:8087"
     * }
     * </pre>
     *
     * @return карта с конфигурационными параметрами, доступными клиенту
     */
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("baseUrl", baseUrl);
    }
}
