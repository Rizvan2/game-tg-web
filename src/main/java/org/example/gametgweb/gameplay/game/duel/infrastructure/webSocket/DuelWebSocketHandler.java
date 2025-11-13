package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.DuelCombatService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.DuelRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * {@code DuelWebSocketHandler} — обработчик WebSocket-соединений для комнат дуэлей.
 * Основная задача — обрабатывать жизненный цикл соединений и сообщения игроков:
 * <ul>
 *   <li>Регистрация и удаление игроков в комнатах</li>
 *   <li>Приём сообщений чата и атак</li>
 *   <li>Взаимодействие с {@link DuelRoomService} и {@link DuelCombatService}</li>
 * </ul>
 *
 * Все активные сессии и состояния юнитов хранятся в {@link DuelRoomService} через {@link RoomSessionRegistry}.
 */
@Slf4j
@Component
public class DuelWebSocketHandler extends TextWebSocketHandler {

    private final DuelRoomService duelRoomService;
    private final DuelCombatService duelCombatService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public DuelWebSocketHandler(DuelRoomService duelRoomService, DuelCombatService duelCombatService) {
        this.duelRoomService = duelRoomService;
        this.duelCombatService = duelCombatService;
    }

    /**
     * Вызывается при успешном подключении нового клиента.
     * <p>
     * Извлекает {@link WebSocketContext} из сессии, содержащий имя игрока и код комнаты.
     * Если контекст отсутствует или не указан {@code gameCode}, соединение закрывается с ошибкой.
     * Иначе игрок добавляется в комнату через {@link DuelRoomService#playerJoin}.
     *
     * @param session активная WebSocket-сессия игрока
     * @throws Exception при ошибке инициализации соединения
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) {
            closeBadSession(session);
            return;
        }
        duelRoomService.playerJoin(ctx, session);
    }

    /**
     * Вызывается при закрытии WebSocket-сессии.
     * <p>
     * Удаляет игрока из комнаты через {@link DuelRoomService#playerLeave}.
     *
     * @param session закрытая WebSocket-сессия
     * @param status  статус закрытия соединения
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var ctx = WebSocketContext.from(session);
        if (ctx != null) {
            duelRoomService.playerLeave(ctx, session);
        }
    }

    /**
     * Обрабатывает входящие текстовые сообщения от игроков.
     * <p>
     * Сообщения должны быть в формате JSON с полем {@code type}:
     * <ul>
     *   <li>{@code "chat"} — текстовое сообщение, рассылается всем игрокам комнаты</li>
     *   <li>{@code "attack"} — атака, обрабатывается через {@link DuelCombatService}</li>
     * </ul>
     *
     * @param session активная WebSocket-сессия
     * @param message текстовое сообщение от игрока
     * @throws Exception если произошла ошибка парсинга или обработки сообщения
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) return;

        JsonNode payload = mapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : null;

        switch (type) {
            case "chat" -> {
                String text = payload.has("message") ? payload.get("message").asText() : "";
                duelRoomService.broadcastChat(ctx, text);
            }
            case "attack" -> handleAttack(ctx, payload);
        }
    }

    /**
     * Обрабатывает действие "атака" от игрока.
     * 1. Валидирует полученный JSON.
     * 2. Преобразует строку в enum Body.
     * 3. Передает данные в сервис дуэли.
     * 4. Возвращает результат или сообщение "ожидания соперника".
     */
    private void handleAttack(WebSocketContext ctx, JsonNode payload) {
        String gameCode = ctx.gameCode();
        String player = ctx.playerName();

        try {
            Body body = extractBody(payload, gameCode, player);
            if (body == null) return;

            processAttackAndRespond(gameCode, player, body);
        } catch (Exception e) {
            handleServerError(gameCode, player, e);
        }
    }

    /**
     * Извлекает и валидирует выбранную часть тела из JSON.
     * Отправляет сообщение об ошибке, если структура неверна.
     */
    private Body extractBody(JsonNode payload, String gameCode, String player) throws IOException {
        if (payload == null || !payload.hasNonNull("body")) {
            sendError(gameCode, player, "Missing 'body' in attack payload");
            return null;
        }

        try {
            return Body.valueOf(payload.get("body").asText().toUpperCase());
        } catch (IllegalArgumentException ex) {
            sendError(gameCode, player, "Invalid body: " + payload.get("body").asText());
            return null;
        }
    }

    /**
     * Вызывает доменный сервис дуэли и реагирует на его результат.
     */
    private void processAttackAndRespond(String gameCode, String player, Body body) throws Exception {
        String resultJson = duelCombatService.processAttack(gameCode, player, body);

        if (resultJson != null) {
            duelRoomService.broadcast(gameCode, resultJson); // оба игрока выбрали — отправляем результат боя
        } else {
            var info = Map.of("type", "info", "message", "Move registered. Waiting for opponent...");
            duelRoomService.sendToPlayer(gameCode, player, mapper.writeValueAsString(info));
        }
    }

    /**
     * Обрабатывает серверные исключения.
     */
    private void handleServerError(String gameCode, String player, Exception e) {
        log.error("Error processing attack", e);
        try {
            sendError(gameCode, player, "Server error during attack processing");
        } catch (IOException ex) {
            log.error("Failed to send error message to player", ex);
        }
    }

    /**
     * Универсальная отправка сообщения об ошибке игроку.
     */
    private void sendError(String gameCode, String player, String message) throws IOException {
        var err = Map.of("type", "error", "message", message);
        duelRoomService.sendToPlayer(gameCode, player, mapper.writeValueAsString(err));
    }

    /**
     * Закрывает соединение с ошибкой, если отсутствует обязательный параметр {@code gameCode}.
     *
     * @param session WebSocket-сессия для закрытия
     * @throws IOException при сбое закрытия соединения
     */
    private void closeBadSession(WebSocketSession session) throws IOException {
        session.close(CloseStatus.BAD_DATA.withReason("Missing gameCode parameter"));
    }
}
