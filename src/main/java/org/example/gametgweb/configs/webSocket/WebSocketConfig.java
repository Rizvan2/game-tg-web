package org.example.gametgweb.configs.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DuelWebSocketHandler duelWebSocketHandler;

    @Autowired
    public WebSocketConfig(DuelWebSocketHandler duelWebSocketHandler) {
        this.duelWebSocketHandler = duelWebSocketHandler;
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(duelWebSocketHandler, "/ws/duel")
                .setAllowedOrigins("*"); // для теста разрешаем все источники
    }
}
