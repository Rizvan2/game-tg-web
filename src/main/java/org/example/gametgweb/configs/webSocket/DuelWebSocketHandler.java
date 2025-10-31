package org.example.gametgweb.configs.webSocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

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
    private static final String ATTR_PLAYER_NAME = "PLAYER_NAME";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ScheduledFuture<?>>> pendingLeaveTasks = new ConcurrentHashMap<>();
    private static final long RELOAD_GRACE_MS = 3000L;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lastLeaveAt = new ConcurrentHashMap<>();

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
        if (playerName != null) {
            session.getAttributes().put(ATTR_PLAYER_NAME, playerName);
        }

        // Если для этого игрока был запланирован «вышел», отменим его и не будем слать «зашёл»
        boolean cancelledLeave = false;
        if (playerName != null) {
            ConcurrentHashMap<String, ScheduledFuture<?>> roomLeaves = pendingLeaveTasks.get(gameCode);
            if (roomLeaves != null) {
                ScheduledFuture<?> f = roomLeaves.remove(playerName);
                if (f != null) {
                    f.cancel(false);
                    cancelledLeave = true;
                }
            }
        }

        // Если недавно фиксировали выход этого игрока, подавим «зашёл»
        if (playerName != null) {
            Long t = lastLeaveAt.computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>()).get(playerName);
            if (t != null && (System.currentTimeMillis() - t) < RELOAD_GRACE_MS) {
                return;
            }
        }

        if (!cancelledLeave) {
            // Отложим «зашёл» на 2с и убедимся, что у игрока есть открытая сессия в комнате
            scheduler.schedule(() -> {
                Set<WebSocketSession> sessions = gameSessions.get(gameCode);
                if (sessions == null) return;
                boolean stillHere = sessions.stream()
                        .filter(WebSocketSession::isOpen)
                        .anyMatch(s -> playerName != null && playerName.equals(s.getAttributes().get(ATTR_PLAYER_NAME)));
                if (stillHere) {
                    broadcast(gameCode, (playerName != null ? playerName : "Игрок") + " подключился к комнате " + gameCode + "!");
                }
            }, 2000, TimeUnit.MILLISECONDS);
        }
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
        String playerName = (String) session.getAttributes().get(ATTR_PLAYER_NAME);

        if (code instanceof String gameCode) {
            Set<WebSocketSession> sessions = gameSessions.get(gameCode);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameCode);
                } else if (playerName != null) {
                    // Отложим «вышел» на 2с и проверим, что у игрока не осталось открытых сессий
                    // Фиксируем время выхода сразу, чтобы новый вход мог подавить «зашёл»
                    lastLeaveAt
                            .computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>())
                            .put(playerName, System.currentTimeMillis());

                    ScheduledFuture<?> leaveFuture = scheduler.schedule(() -> {
                        boolean stillConnected = sessions.stream()
                                .filter(WebSocketSession::isOpen)
                                .anyMatch(s -> playerName.equals(s.getAttributes().get(ATTR_PLAYER_NAME)));
                        if (!stillConnected) {
                            broadcast(gameCode, playerName + " вышел из комнаты " + gameCode + "!");
                        }
                        ConcurrentHashMap<String, ScheduledFuture<?>> roomLeaves = pendingLeaveTasks.get(gameCode);
                        if (roomLeaves != null) roomLeaves.remove(playerName);
                    }, 2000, TimeUnit.MILLISECONDS);

                    pendingLeaveTasks.computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>()).put(playerName, leaveFuture);
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

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }
}
