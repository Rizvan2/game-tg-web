package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelRoomService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.AttackMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.ChatMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.ErrorMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.InfoMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat.DuelCombatService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

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
    private final MessageDispatcherService messageDispatcherService;
    private final ObjectMapper mapper;

    @Autowired
    public DuelWebSocketHandler(DuelRoomService duelRoomService, DuelCombatService duelCombatService, MessageDispatcherService messageDispatcherService, ObjectMapper mapper) {
        this.duelRoomService = duelRoomService;
        this.duelCombatService = duelCombatService;
        this.messageDispatcherService = messageDispatcherService;
        this.mapper = mapper;
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

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) return;

        JsonNode payload = mapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : null;

        switch (type) {
            case "chat" -> {
                String text = payload.has("message") ? payload.get("message").asText() : "";
                ChatMessageDTO chatDto = new ChatMessageDTO(ctx.playerName(), text);
                messageDispatcherService.broadcastChat(
                        ctx.gameCode(),
                        chatDto.getEffectiveSender(), // <--- здесь
                        chatDto.getMessage()
                );
            }
            case "attack" -> handleAttack(ctx, payload);
        }
    }

    private void handleAttack(WebSocketContext ctx, JsonNode payload) {
        String gameCode = ctx.gameCode();
        String player = ctx.playerName();

        try {
            AttackMessageDTO attack = mapper.treeToValue(payload, AttackMessageDTO.class);
            Body body = attack.bodyEnum();
            if (body == null) return;

            processAttackAndRespond(gameCode, player, body);
        } catch (Exception e) {
            handleServerError(gameCode, player, e);
        }
    }

    private void processAttackAndRespond(String gameCode, String player, Body body) throws Exception {
        String resultJson = duelCombatService.processAttack(gameCode, player, body);

        if (resultJson != null) {
            broadcastRoundResult(gameCode, player, resultJson);
            duelRoomService.sendUnitsState(gameCode);
        } else {
            sendWaitingMessage(gameCode, player);
        }
    }

    private void broadcastRoundResult(String gameCode, String player, String resultJson) {
        messageDispatcherService.broadcastChat(gameCode, player, resultJson);
    }

    private void sendWaitingMessage(String gameCode, String player) throws Exception {
        InfoMessageDTO info = new InfoMessageDTO("Move registered. Waiting for opponent...");
        messageDispatcherService.sendToPlayer(gameCode, player, new ObjectMapper().writeValueAsString(info));
    }

    private void handleServerError(String gameCode, String player, Exception e) {
        log.error("Error processing attack", e);
        try {
            sendError(gameCode, player, "Server error during attack processing");
        } catch (IOException ex) {
            log.error("Failed to send error message to player", ex);
        }
    }

    private void sendError(String gameCode, String player, String message) throws IOException {
        ErrorMessageDTO err = new ErrorMessageDTO(message);
        messageDispatcherService.sendToPlayer(gameCode, player, mapper.writeValueAsString(err));
    }

    private void closeBadSession(WebSocketSession session) throws IOException {
        session.close(CloseStatus.BAD_DATA.withReason("Missing gameCode parameter"));
    }
}
