package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.webSocket.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.JoinLeaveScheduler;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.SessionRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DuelRoomService — сервис управления комнатами дуэлей и игроками.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>Регистрацию и удаление WebSocket-сессий игроков;</li>
 *     <li>Управление юнитами игроков в комнате;</li>
 *     <li>Логику присоединения и выхода через {@link JoinLeaveScheduler}.</li>
 * </ul>
 */
@Component
@Slf4j
public class DuelRoomService {

    private final PlayerRepositoryImpl playerService;
    private final UnitRegistryService unitRegistry;
    private final SessionRegistryService sessionRegistry;
    private final PlayerLifecycleService playerLifecycleService;
    private final MessageDispatcherService messageDispatcher;
    private final Map<String, List<String>> playerOrderMap = new ConcurrentHashMap<>();

    @Autowired
    public DuelRoomService(SessionRegistryService sessionRegistry,
                           PlayerRepositoryImpl playerService,
                           UnitRegistryService unitRegistry,
                           PlayerLifecycleService playerLifecycleService, MessageDispatcherService messageDispatcher) {
        this.sessionRegistry = sessionRegistry;
        this.playerService = playerService;
        this.unitRegistry = unitRegistry;
        this.playerLifecycleService = playerLifecycleService;
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * Добавляет игрока в комнату дуэли.
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.addSession(ctx.gameCode(), session);

        // Добавляем игрока в список порядка входа
        playerOrderMap.computeIfAbsent(ctx.gameCode(), k -> new ArrayList<>());
        List<String> order = playerOrderMap.get(ctx.gameCode());
        if (!order.contains(ctx.playerName())) {
            order.add(ctx.playerName());
        }
        Player playerEntity = playerService.findByUsername(ctx.playerName());
        if (playerEntity != null) {
            Unit unit = playerEntity.getActiveUnit();
            if (unit != null) {
                unitRegistry.registerUnit(ctx.gameCode(), ctx.playerName(), unit);
                log.info("Юнит {} зарегистрирован для игрока {} в комнате {}",
                        unit.getName(), ctx.playerName(), ctx.gameCode());
            }
        }

        // 👉 Сразу отправляем всем обновлённое состояние юнитов
        sendUnitsState(ctx.gameCode());

        playerLifecycleService.handleJoin(ctx);
    }


    /**
     * Удаляет игрока из комнаты.
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.removeSession(ctx.gameCode(), session);

        // Удаляем игрока из списка порядка входа
        List<String> order = playerOrderMap.get(ctx.gameCode());
        if (order != null) {
            order.remove(ctx.playerName());
            if (order.isEmpty()) playerOrderMap.remove(ctx.gameCode());
        }

        playerLifecycleService.handleLeave(ctx);
    }

    // Метод для получения порядка
    public List<String> getPlayerOrder(String gameCode) {
        return playerOrderMap.getOrDefault(gameCode, List.of());
    }

    private void sendUnitsState(String gameCode) {
        Set<WebSocketSession> sessions = sessionRegistry.getSessions(gameCode);

        // Получаем список игроков в порядке входа (добавьте такой список в DuelRoomService)
        List<String> playerOrder = getPlayerOrder(gameCode); // например, ["Alice", "Bob"]

        List<Map<String, Object>> units = new ArrayList<>();
        for (String playerName : playerOrder) {
            Unit unit = unitRegistry.getUnit(gameCode, playerName);
            if (unit != null) {
                Map<String, Object> unitMap = new HashMap<>();
                unitMap.put("player", playerName);
                unitMap.put("unitName", unit.getName());
                unitMap.put("hp", unit.getHealth());
                unitMap.put("hpMax", unit.getMaxHealth());
                unitMap.put("imagePath", unit.getImagePath());
                units.add(unitMap);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "UNITS_STATE");
        payload.put("units", units);

        for (WebSocketSession session : sessions) {
            messageDispatcher.send(session, payload);
        }
    }

    /**
     * Возвращает копию набора активных сессий для комнаты.
     *
     * @param gameCode код комнаты
     * @return множество WebSocket-сессий; если комнаты нет, возвращает пустой набор
     */
    public Set<WebSocketSession> getSessions(String gameCode) {
        // возвращаем mutable копию
        return sessionRegistry.getSessions(gameCode);
    }

    /**
     * Возвращает юнита игрока по имени в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return юнит игрока или null
     */
    public Unit getUnit(String gameCode, String playerName) {
        return unitRegistry.getUnit(gameCode, playerName);
    }
}
