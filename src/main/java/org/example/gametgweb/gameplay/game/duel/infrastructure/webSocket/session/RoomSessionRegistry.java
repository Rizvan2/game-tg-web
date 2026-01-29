package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RoomSessionRegistry — потокобезопасный реестр активных WebSocket-сессий и игровых юнитов по комнатам.
 *
 * <p>Основные задачи:
 * <ul>
 *     <li>Хранение WebSocket-сессий игроков по коду комнаты;</li>
 *     <li>Хранение игровых юнитов игроков по коду комнаты;</li>
 *     <li>Обеспечение безопасной рассылки сообщений всем игрокам или конкретному игроку;</li>
 *     <li>Удаление сессий и очистка комнат, когда они становятся пустыми.</li>
 * </ul>
 *
 * <p>Использует потокобезопасные коллекции {@link ConcurrentHashMap} и {@link ConcurrentHashMap#newKeySet()}.
 */
@Slf4j
@Component
public class RoomSessionRegistry {

    /**
     * Активные WebSocket-сессии игроков, сгруппированные по коду комнаты.
     * Key — gameCode, Value — набор сессий игроков в комнате.
     */
    private final ConcurrentHashMap<String, Set<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();

    // ============================================================
    // =============== Работа с WebSocket-сессиями =================
    // ============================================================

    /**
     * Добавляет WebSocket-сессию игрока в комнату.
     *
     * @param gameCode код комнаты
     * @param session  WebSocket-сессия игрока
     */
    public void addSession(String gameCode, WebSocketSession session) {
        gameSessions
                .computeIfAbsent(gameCode, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        log.info("Добавлена сессия {} в комнату {}", session.getId(), gameCode);
        logPlayersInRoom(gameCode);
    }

    /**
     * Удаляет WebSocket-сессию из комнаты.
     *
     * @param gameCode код комнаты
     * @param session  WebSocket-сессия игрока
     */
    public void removeSession(String gameCode, WebSocketSession session) {
        safeRemoveSession(gameCode, session);
        logPlayersInRoom(gameCode);
    }

    /**
     * Возвращает набор ОРИГИНАЛЬНЫХ сессий (НЕ копия)
     */
    public Set<WebSocketSession> getSessionsRaw(String gameCode) {
        return gameSessions.getOrDefault(gameCode, ConcurrentHashMap.newKeySet());
    }

    private void cleanupClosedSessions(String gameCode) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        if (sessions == null) return;

        sessions.removeIf(s -> !s.isOpen());

        if (sessions.isEmpty()) {
            log.info("Комната {} временно без активных сессий", gameCode);
            return;
        }

        logPlayersInRoom(gameCode);
    }


    /**
     * Находит сессию игрока по имени.
     */
    public WebSocketSession getSessionByPlayer(String gameCode, String playerName) {
        return getSessionsRaw(gameCode)
                .stream()
                .filter(s -> playerName.equals(s.getAttributes().get("PLAYER_NAME")))
                .findFirst()
                .orElse(null);
    }

    /**
     * Безопасно удаляет сессию.
     */
    private void safeRemoveSession(String gameCode, WebSocketSession session) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        if (sessions == null) return;
        sessions.remove(session);
        log.info("Удалена сессия {} из комнаты {}", session.getId(), gameCode);

        cleanupClosedSessions(gameCode);
    }

    /**
     * Рассылает сообщение всем игрокам в комнате.
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
        log.debug("📤 Отправляем сообщение в комнату {} всем сессиям: {}", gameCode, message);

        sessions.removeIf(s -> !s.isOpen());

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
     * Отправляет сообщение конкретному игроку в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @param message    текст сообщения
     */
    public void sendToPlayer(String gameCode, String playerName, String message) {
        log.info("🔥 sendToPlayer вызван: gameCode={}, playerName={}", gameCode, playerName);

        Set<WebSocketSession> sessions = getSessions(gameCode);
        if (sessions == null || sessions.isEmpty()) {
            log.warn("⚠️ Нет сессий для комнаты {}", gameCode);
            return;
        }

        // Удаляем закрытые сессии
        sessions.removeIf(s -> !s.isOpen());

        sessions.stream()
                .filter(s -> playerName.equals(s.getAttributes().get("PLAYER_NAME")))
                .forEach(s -> {
                    log.info("✉️ Отправляем сообщение игроку {} (sessionId={})", playerName, s.getId());
                    try {
                        s.sendMessage(new TextMessage(message));
                        log.info("✅ Сообщение отправлено игроку {}", playerName);
                    } catch (IOException e) {
                        log.error("❌ Ошибка при отправке игроку {}: {}", playerName, e.getMessage());
                        safeRemoveSession(gameCode, s);
                    }
                });
    }

    /**
     * Возвращает копию набора активных сессий для комнаты.
     *
     * @param gameCode код комнаты
     * @return множество WebSocket-сессий; если комнаты нет, возвращает пустой набор
     */
    public Set<WebSocketSession> getSessions(String gameCode) {
        var sessions = gameSessions.get(gameCode);
        // возвращаем mutable копию
        return sessions != null ? new HashSet<>(sessions) : new HashSet<>();
    }

    public void replaceSession(String gameCode, String playerName, WebSocketSession newSession) {
        WebSocketSession oldSession = getSessionByPlayer(gameCode, playerName); // находим старую
        if (oldSession != null) {
            removeSession(gameCode, oldSession); // удаляем старую
        }
        addSession(gameCode, newSession); // добавляем новую
        log.info("Сессия обновлена для игрока {} в комнате {} (реконнект)", playerName, gameCode);
    }

    /**
     * Логирует всех активных игроков в комнате.
     *
     * @param gameCode код комнаты
     */
    public void logPlayersInRoom(String gameCode) {
        Set<WebSocketSession> sessions = gameSessions.getOrDefault(gameCode, Set.of());
        if (sessions.isEmpty()) {
            log.info("Комната {} пуста", gameCode);
            return;
        }

        String players = sessions.stream()
                .map(s -> (String) s.getAttributes().get("PLAYER_NAME"))
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + ", " + b)
                .orElse("неизвестные игроки");

        log.info("Комната {} содержит игроков: {}", gameCode, players);
    }
}
