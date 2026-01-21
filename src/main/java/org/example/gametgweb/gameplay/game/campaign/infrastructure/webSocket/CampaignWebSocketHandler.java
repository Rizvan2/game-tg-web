package org.example.gametgweb.gameplay.game.campaign.infrastructure.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity.CampaignEntity;
import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.repository.CampaignService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat.CombatService;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * WebSocket-обработчик для боёв в режиме кампании.
 * <p>
 * Отвечает за приём и обработку событий от клиента (игрока),
 * делегируя игровую механику в {@link CombatService}.
 * Сохраняет состояние активных кампаний в {@link CampaignSessionRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CampaignService campaignService;
    private final CampaignSessionRegistry registry;
    private final PlayerRepositoryImpl playerService;
    private final CombatService combatService;

    /**
     * При подключении игрока к WebSocket-серверу.
     * Добавляет его сессию в {@link CampaignSessionRegistry}.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String playerName = (String) session.getAttributes().get("PLAYER_NAME");
        if (playerName == null) {
            log.error("Ошибка аутентификации: игрок не найден для сессии {}", session.getId());
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Игрок не найден"));
            } catch (IOException ignored) {}
            return;
        }

        registry.addSession(playerName, session);
        log.info("⚔ Игрок подключился к кампании: {} ({})", session.getId(), playerName);
    }

    /**
     * Обрабатывает входящие сообщения от клиента.
     * Поддерживаемые действия:
     * <ul>
     *     <li><b>start</b> — начало новой кампании</li>
     *     <li><b>attack</b> — атака игрока</li>
     *     <li><b>enemyTurn</b> — ход противника</li>
     * </ul>
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonNode node = mapper.readTree(message.getPayload());
        String action = node.has("action") ? node.get("action").asText() : "";

        log.info("📩 Получено действие '{}' от {}", action, session.getId());

        try {
            switch (action) {
                case "start" -> handleStart(session);
                case "attack" -> handleAttack(session, node);
                case "enemyTurn" -> handleEnemyTurn(session, node);
                default -> sendError(session, "Неизвестное действие: " + action);
            }
        } catch (IllegalArgumentException e) {
            sendError(session, "Неверный параметр: " + e.getMessage());
            log.warn("Ошибка обработки сообщения от {}: {}", session.getId(), e.getMessage());
        } catch (IllegalStateException e) {
            sendError(session, e.getMessage());
            log.warn("Бизнес-ошибка для {}: {}", session.getId(), e.getMessage());
        } catch (Exception e) {
            sendError(session, "Внутренняя ошибка сервера");
            log.error("Ошибка при обработке сообщения от {}", session.getId(), e);
        }
    }

    /**
     * При отключении игрока удаляет сессию из реестра.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerName = getCurrentPlayerName(session);
        log.info("❌ Игрок {} отключился: {}", playerName, session.getId());
        registry.removeSession(playerName, session);
    }

    /**
     * Начинает новую кампанию для игрока.
     */
    private void handleStart(WebSocketSession session) throws IOException {
        String playerName = (String) session.getAttributes().get("PLAYER_NAME");
        if (playerName == null) {
            sendError(session, "Ошибка аутентификации: игрок не найден");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Игрок не найден"));
            return;
        }

        Player playerEntity = playerService.findByUsername(playerName);
        CampaignEntity campaignEntity = campaignService.startCampaign(playerEntity, "Turk Warrior");
        registry.putCampaign(playerName, campaignEntity);
        registry.addSession(playerName, session);

        registry.broadcast(playerName, mapper.writeValueAsString(Map.of(
                "message", "⚔ Кампания начата!",
                "player", campaignEntity.getPlayerUnitEntity(),
                "enemy", campaignEntity.getEnemyUnitEntity()
        )));
    }

    /**
     * Обрабатывает атаку игрока.
     */
    private void handleAttack(WebSocketSession session, JsonNode node) throws IOException {
        handleCombatTurn(session, node, true);
    }

    /**
     * Обрабатывает ход противника.
     */
    private void handleEnemyTurn(WebSocketSession session, JsonNode node) throws IOException {
        handleCombatTurn(session, node, false);
    }

    /**
     * Универсальный метод для атаки (игрока или врага).
     */
    private void handleCombatTurn(WebSocketSession session, JsonNode node, boolean isPlayerTurn) throws IOException {
        String playerName = getCurrentPlayerName(session);
        CampaignEntity campaignEntity = registry.getCampaign(playerName);
        if (campaignEntity == null) throw new IllegalStateException("Кампания не начата для " + playerName);

        Body body = parseBody(node.has("body") ? node.get("body").asText() : "BODY");

        PlayerUnit attacker = isPlayerTurn ?
                PlayerUnitMapper.toDomain(campaignEntity.getPlayerUnitEntity()) :
                PlayerUnitMapper.toDomain(campaignEntity.getEnemyUnitEntity());
        PlayerUnit defender = isPlayerTurn ?
                PlayerUnitMapper.toDomain(campaignEntity.getEnemyUnitEntity()) :
                PlayerUnitMapper.toDomain(campaignEntity.getPlayerUnitEntity());

        String message = "adasd";
//        String message = combatService.attack(attacker, defender, body);

        registry.broadcast(playerName, mapper.writeValueAsString(Map.of(
                "message", message,
                "player", campaignEntity.getPlayerUnitEntity(),
                "enemy", campaignEntity.getEnemyUnitEntity()
        )));
    }

    /**
     * Преобразует строковое значение в enum {@link Body}.
     */
    private Body parseBody(String name) {
        try {
            return Body.valueOf(name.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверная часть тела: " + name);
        }
    }

    /**
     * Извлекает имя игрока из атрибутов WebSocket-сессии.
     */
    private String getCurrentPlayerName(WebSocketSession session) {
        String playerName = (String) session.getAttributes().get("PLAYER_NAME");
        if (playerName == null) throw new IllegalStateException("Игрок не найден в сессии " + session.getId());
        return playerName;
    }

    /**
     * Отправляет сообщение об ошибке клиенту.
     */
    private void sendError(WebSocketSession session, String error) throws IOException {
        var json = mapper.createObjectNode();
        json.put("error", error);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(json)));
    }
}
