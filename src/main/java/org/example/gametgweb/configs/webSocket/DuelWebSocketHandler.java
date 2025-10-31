package org.example.gametgweb.configs.webSocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code DuelWebSocketHandler} — обработчик WebSocket-соединений,
 * обеспечивающий обмен сообщениями между игроками внутри одной игровой комнаты.
 * <p>
 * Используется потокобезопасная структура данных для хранения подключений,
 * что позволяет безопасно работать с множеством одновременных подключений.
 */
@Component
public class DuelWebSocketHandler extends TextWebSocketHandler {

    /**
     * Потокобезопасная карта, где:
     * <ul>
     *     <li>ключ — код игровой комнаты ({@code gameCode});</li>
     *     <li>значение — множество WebSocket-сессий игроков в этой комнате.</li>
     * </ul>
     * Используется {@link ConcurrentHashMap} и {@link ConcurrentHashMap#newKeySet()} для потокобезопасности.
     */
    private final ConcurrentHashMap<String, Set<WebSocketSession>> gameSessions = new ConcurrentHashMap<>();
    private static final String ATTR_GAME_CODE = "GAME_CODE";

    /**
     * Метод вызывается автоматически при установлении нового WebSocket-соединения.
     * <p>
     * Извлекает код комнаты из query-параметра {@code gameCode},
     * добавляет сессию игрока в соответствующую комнату
     * и уведомляет всех участников комнаты о подключении нового игрока.
     *
     * @param session объект {@link WebSocketSession}, представляющий соединение клиента
     * @throws Exception если при инициализации соединения возникла ошибка
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String gameCode = extractQueryParam(query, "gameCode");
        if (gameCode == null || gameCode.isEmpty()) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing gameCode parameter"));
            return;
        }

        // Запоминаем комнату в атрибутах сессии
        session.getAttributes().put(ATTR_GAME_CODE, gameCode);

        // Создаёт новую комнату при необходимости и добавляет сессию игрока
        gameSessions.computeIfAbsent(gameCode, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Имя игрока из аутентификации (если доступно)
        String playerName = resolvePlayerName(session.getPrincipal());

        // Рассылаем сообщение всем участникам комнаты
        broadcast(gameCode, (playerName != null ? (playerName) : "Игрок") + " подключился к комнате " + gameCode + "!");
    }

    /**
     * Метод вызывается при закрытии WebSocket-соединения.
     * <p>
     * Удаляет сессию игрока из всех комнат, где он мог находиться,
     * и записывает лог о закрытии соединения.
     *
     * @param session объект {@link WebSocketSession}, представляющий закрывающееся соединение
     * @param status  причина закрытия соединения
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object code = session.getAttributes().get(ATTR_GAME_CODE);
        if (code instanceof String gameCode) {
            Set<WebSocketSession> sessions = gameSessions.get(gameCode);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameCode);
                }
            }
        } else {
            gameSessions.values().forEach(sessions -> sessions.remove(session));
        }
        System.out.println("WebSocket закрыт: " + session.getId() + ", статус: " + status);
    }

    /**
     * Отправляет текстовое сообщение всем активным игрокам в указанной комнате.
     * <p>
     * Каждое соединение проверяется на открытость перед отправкой.
     * Ошибки при отправке сообщений логируются, но не прерывают процесс рассылки.
     *
     * @param gameCode уникальный код игровой комнаты
     * @param message  текст сообщения, отправляемого всем участникам комнаты
     */
    public void broadcast(String gameCode, String message) {
        Set<WebSocketSession> sessions = gameSessions.get(gameCode);
        if (sessions == null) return;

        sessions.forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    System.err.println("Ошибка отправки сообщения: " + e.getMessage());
                }
            }
        });
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || key == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String k = pair.substring(0, idx);
            String v = pair.substring(idx + 1);
            if (key.equals(k)) {
                return v;
            }
        }
        return null;
    }

    private String resolvePlayerName(Principal principal) {
        if (principal == null) return null;
        try {
            // principal.getName() уже должен возвращать username
            String name = principal.getName();
            return (name != null && !name.isBlank()) ? name : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
