package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

@Service
@Slf4j
public class SessionRegistryService {

    private final RoomSessionRegistry registry;

    public SessionRegistryService(RoomSessionRegistry registry) {
        this.registry = registry;
    }

    public void addSession(String gameCode, WebSocketSession session) {
        registry.addSession(gameCode, session);
        log.info("Сессия добавлена для комнаты {}", gameCode);
    }

    public void removeSession(String gameCode, WebSocketSession session) {
        registry.removeSession(gameCode, session);
        log.info("Сессия удалена из комнаты {}", gameCode);
    }
    /**
     * Возвращает копию набора активных сессий для комнаты.
     *
     * @param gameCode код комнаты
     * @return множество WebSocket-сессий; если комнаты нет, возвращает пустой набор
     */
    public Set<WebSocketSession> getSessions(String gameCode) {
        // возвращаем mutable копию
        return registry.getSessions(gameCode);
    }
}
