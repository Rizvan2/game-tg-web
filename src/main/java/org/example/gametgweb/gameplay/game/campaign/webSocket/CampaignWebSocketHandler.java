package org.example.gametgweb.gameplay.game.campaign.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.example.gametgweb.gameplay.game.Body;
import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.entity.Unit;
import org.example.gametgweb.services.CampaignService;
import org.example.gametgweb.services.PlayerService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CampaignService campaignService;
    private final CampaignSessionRegistry registry;
    private final PlayerService playerService;

    /**
     * Сессии игроков: sessionId → campaign
     */
    private final Map<String, Campaign> campaigns = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Достаём имя игрока из атрибутов сессии
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
     * Обрабатывает входящие WebSocket-сообщения от клиента.
     * <p>
     * Поддерживаемые действия:
     * <ul>
     *     <li><b>"start"</b> — начало новой кампании;</li>
     *     <li><b>"attack"</b> — атака игрока (ожидается параметр <code>body</code> в JSON);</li>
     *     <li><b>"enemyTurn"</b> — ход противника (опционально может содержать <code>body</code> для целевой части тела);</li>
     * </ul>
     * <p>
     * В случае неизвестного действия или ошибок в параметрах отправляет клиенту сообщение об ошибке.
     *
     * @param session активная {@link WebSocketSession} игрока;
     * @param message входящее сообщение клиента в формате JSON;
     * @throws IOException если возникает ошибка при чтении или отправке сообщения.
     *
     * @see #handleStart(WebSocketSession)
     * @see #handleAttack(WebSocketSession, JsonNode)
     * @see #handleEnemyTurn(WebSocketSession, JsonNode)
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
                case "enemyTurn" -> handleEnemyTurn(session, node); // ✅ передаём node
                default -> sendError(session, "Неизвестное действие: " + action);
            }
        } catch (IllegalArgumentException e) {
            // чаще всего сюда попадёт ошибка при парсинге Body.valueOf(...)
            sendError(session, "Неверный параметр: " + e.getMessage());
            log.warn("Ошибка обработки сообщения от {}: {}", session.getId(), e.getMessage());
        } catch (IllegalStateException e) {
            // бизнес-ошибки (например, игрок не найден, кампания не начата)
            sendError(session, e.getMessage());
            log.warn("Бизнес-ошибка для {}: {}", session.getId(), e.getMessage());
        } catch (Exception e) {
            sendError(session, "Внутренняя ошибка сервера");
            log.error("Ошибка при обработке сообщения от {}", session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerName = getCurrentPlayerName(session);
        log.info("❌ Игрок {} отключился: {}", playerName, session.getId());
        registry.removeSession(playerName, session);
    }

    private void handleStart(WebSocketSession session) throws IOException {
        String playerName = (String) session.getAttributes().get("PLAYER_NAME");
        if (playerName == null) {
            sendError(session, "Ошибка аутентификации: игрок не найден");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Игрок не найден"));
            return;
        }

        // Можно добавить метод для поиска PlayerEntity по имени
        PlayerEntity playerEntity = playerService.findByUsername(playerName);
        Campaign campaign = campaignService.startCampaign(playerEntity, "Turk Warrior");
        registry.putCampaign(playerName, campaign);
        registry.addSession(playerName, session);
        // 👇 Вот это сообщение надо отправить сразу
        registry.broadcast(playerName, mapper.writeValueAsString(Map.of(
                "message", "⚔ Кампания начата!",
                "player", campaign.getPlayerUnit(),
                "enemy", campaign.getEnemyUnit()
        )));
    }



    /**
     * Обрабатывает команду "attack" (атака игрока).
     */
    private void handleAttack(WebSocketSession session, JsonNode node) throws IOException {
        handleCombatTurn(session, node, true);
    }

    /**
     * Обрабатывает команду "enemyTurn" (ход противника).
     */
    private void handleEnemyTurn(WebSocketSession session, JsonNode node) throws IOException {
        handleCombatTurn(session, node, false);
    }

    private void handleCombatTurn(WebSocketSession session, JsonNode node, boolean isPlayerTurn) throws IOException {
        String playerName = getCurrentPlayerName(session);
        Campaign campaign = registry.getCampaign(playerName);
        if (campaign == null) throw new IllegalStateException("Кампания не начата для " + playerName);

        Body body = parseBody(node.has("body") ? node.get("body").asText() : "BODY");

        Unit attacker = isPlayerTurn ? campaign.getPlayerUnit() : campaign.getEnemyUnit();
        Unit defender = isPlayerTurn ? campaign.getEnemyUnit() : campaign.getPlayerUnit();

        defender.takeDamage(body, attacker.getDamage());
        campaignService.saveCampaign(campaign);

        String message = attacker.getName() + " атакует " + defender.getName() + " в " + body.name().toLowerCase();
        registry.broadcast(playerName, mapper.writeValueAsString(Map.of(
                "message", message,
                "player", campaign.getPlayerUnit(),
                "enemy", campaign.getEnemyUnit()
        )));
    }


    /**
     * Вспомогательный метод — получает кампанию для сессии или бросает IllegalStateException,
     * если кампания не найдена (например, не была запущена).
     *
     * @param session WebSocket сессия
     * @return Campaign связанная с сессией
     * @throws IllegalStateException если кампания не начата
     */
    private Campaign requireCampaign(WebSocketSession session) {
        Campaign campaign = campaigns.get(session.getId());
        if (campaign == null) {
            throw new IllegalStateException("Кампания не начата для сессии " + session.getId());
        }
        return campaign;
    }

    /**
     * Безопасно пытается распарсить Body по имени. Бросает IllegalArgumentException при неверном значении.
     *
     * @param name имя части тела (например, "HEAD", "LEFT_ARM")
     * @return Body
     * @throws IllegalArgumentException если name не соответствует ни одному значению enum
     */
    private Body parseBody(String name) {
        try {
            return Body.valueOf(name.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Неверная часть тела: " + name);
        }
    }

    // Метод получения имени игрока из WebSocketSession
    private String getCurrentPlayerName(WebSocketSession session) {
        String playerName = (String) session.getAttributes().get("PLAYER_NAME");
        if (playerName == null) throw new IllegalStateException("Игрок не найден в сессии " + session.getId());
        return playerName;
    }

    private void sendState(WebSocketSession session, Campaign campaign, String message) throws IOException {
        var json = mapper.createObjectNode();
        json.put("message", message);
        json.set("player", mapper.valueToTree(campaign.getPlayerUnit()));
        json.set("enemy", mapper.valueToTree(campaign.getEnemyUnit()));
        session.sendMessage(new TextMessage(mapper.writeValueAsString(json)));
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        var json = mapper.createObjectNode();
        json.put("error", error);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(json)));
    }
}
