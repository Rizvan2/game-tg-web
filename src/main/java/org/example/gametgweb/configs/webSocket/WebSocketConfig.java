package org.example.gametgweb.configs.webSocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Конфигурация WebSocket-подключений приложения.
 * <p>
 * Отвечает за регистрацию конечных точек WebSocket и настройку разрешённых источников
 * (доменов), с которых допускаются подключения.
 * </p>
 *
 * <p><b>Основное назначение:</b></p>
 * <ul>
 *     <li>Включает поддержку WebSocket с помощью {@link EnableWebSocket}.</li>
 *     <li>Регистрирует {@link DuelWebSocketHandler} по адресу <code>/ws/duel</code>.</li>
 *     <li>Ограничивает подключение клиентов только указанным в настройках доменом.</li>
 * </ul>
 *
 * <p><b>Важно:</b> значение <code>game.base-url</code> должно быть определено
 * в <code>application.yml</code> или <code>application.properties</code> и указывать
 * на доверенный источник (например, фронтенд-домен или публичный ngrok-URL для теста).</p>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /** Обработчик WebSocket-сессий дуэли между игроками. */
    private final DuelWebSocketHandler duelWebSocketHandler;

    /** Разрешённый источник подключений (берётся из конфигурации). */
    private final String allowedOrigin;

    /**
     * Конструктор конфигурации WebSocket.
     *
     * @param duelWebSocketHandler обработчик соединений для дуэлей.
     * @param allowedOrigin значение параметра {@code game.base-url} из конфигурации приложения,
     *                      указывающее разрешённый источник подключения.
     */
    @Autowired
    public WebSocketConfig(DuelWebSocketHandler duelWebSocketHandler,
                           @Value("${game.base-url}") String allowedOrigin) {
        this.duelWebSocketHandler = duelWebSocketHandler;
        this.allowedOrigin = allowedOrigin;
    }

    /**
     * Регистрирует обработчик WebSocket-соединений и задаёт допустимые источники.
     *
     * @param registry реестр обработчиков WebSocket.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(duelWebSocketHandler, "/ws/duel")
                .setAllowedOrigins(allowedOrigin);
    }
}
