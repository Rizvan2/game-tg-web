package org.example.gametgweb.gameplay.game.Duel.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RoomSessionRegistry — реестр активных WebSocket-сессий игроков по комнатам.
 * <p>
 * Хранит потокобезопасные множества сессий и обеспечивает безопасную работу с ними,
 * включая добавление, удаление и рассылку сообщений.
 */
@Slf4j
@Component
public class RoomSessionRegistry {

    private final ConcurrentHashMap<String, Set<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();

    /**
     * Добавляет сессию в указанную комнату.
     *
     * @param gameCode код комнаты
     * @param session  WebSocket-сессия игрока
     */
    public void addSession(String gameCode, WebSocketSession session) {
        gameSessions.computeIfAbsent(gameCode, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("Добавлена сессия {} в комнату {}", session.getId(), gameCode);
    }

    /**
     * Удаляет сессию из комнаты и очищает комнату, если она пуста.
     *
     * @param gameCode код комнаты
     * @param session  WebSocket-сессия игрока
     */
    public void removeSession(String gameCode, WebSocketSession session) {
        safeRemoveSession(gameCode, session);
    }

    /**
     * Отправляет текстовое сообщение всем открытым сессиям комнаты.
     * Закрытые сессии удаляются из реестра.
     *
     * @param gameCode код комнаты
     * @param message  текст сообщения
     */
    public void broadcast(String gameCode, String message) {
        Set<WebSocketSession> sessions = getSessions(gameCode);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("Комната {} пуста — сообщение не отправлено", gameCode);
            return;
        }

        // Удаляем закрытые сессии
        sessions.removeIf(s -> !s.isOpen());

        // Отправляем сообщение всем открытым сессиям
        sessions.forEach(s -> {
            try {
                s.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Ошибка отправки сообщения (session={}): {}", s.getId(), e.getMessage());
                safeRemoveSession(gameCode, s);
            }
        });
    }

    /**
     * Получает множество сессий для комнаты.
     *
     * @param gameCode код комнаты
     * @return множество сессий или null, если комнаты нет
     */
    private Set<WebSocketSession> getSessions(String gameCode) {
        return gameSessions.get(gameCode);
    }

    /**
     * Безопасно удаляет сессию из комнаты, логирует удаление и очищает комнату, если она пуста.
     *
     * @param gameCode код комнаты
     * @param session  WebSocket-сессия
     */
    private void safeRemoveSession(String gameCode, WebSocketSession session) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        if (sessions == null) return;

        sessions.remove(session);
        log.info("Удалена сессия {} из комнаты {}", session.getId(), gameCode);

        if (sessions.isEmpty()) {
            gameSessions.remove(gameCode);
            log.info("Комната {} пуста — удалена из реестра", gameCode);
        }
    }

    /**
     * Проверяет, пуста ли комната.
     *
     * @param gameCode код комнаты
     * @return true, если в комнате нет сессий
     */
    public boolean isRoomEmpty(String gameCode) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        return sessions == null || sessions.isEmpty();
    }

    /**
     * Проверяет, есть ли открытая сессия у указанного игрока в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return true, если есть хотя бы одна открытая сессия
     */
    public boolean hasOpenConnection(String gameCode, String playerName) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        if (sessions == null) return false;

        return sessions.stream()
                .filter(WebSocketSession::isOpen)
                .anyMatch(s -> playerName.equals(s.getAttributes().get("PLAYER_NAME")));
    }
}
