package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.webSocket.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.SessionRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

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
    private final PlayerOrderService playerOrderService;

    @Autowired
    public DuelRoomService(SessionRegistryService sessionRegistry,
                           PlayerRepositoryImpl playerService,
                           UnitRegistryService unitRegistry,
                           PlayerLifecycleService playerLifecycleService, MessageDispatcherService messageDispatcher, PlayerOrderService playerOrderService) {
        this.sessionRegistry = sessionRegistry;
        this.playerService = playerService;
        this.unitRegistry = unitRegistry;
        this.playerLifecycleService = playerLifecycleService;
        this.messageDispatcher = messageDispatcher;
        this.playerOrderService = playerOrderService;
    }

    /**
     * Обрабатывает подключение игрока к комнате.
     * <p>
     * Определяет, является ли это реконнектом или новым входом,
     * и вызывает соответствующие обработчики.
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        String gameCode = ctx.gameCode();
        String playerName = ctx.playerName();


        if (isReconnect(gameCode, playerName) && playerOrderService.isOffline(gameCode, playerName)) {
            playerOrderService.markOnline(gameCode, playerName);
            handleReconnect(ctx, session, gameCode, playerName);
        } else {
            sessionRegistry.addSession(gameCode, session);
            attachPlayerName(session, playerName);
            handleNewJoin(ctx, gameCode, playerName);
        }

    }

    /**
     * Проверяет, находится ли игрок уже в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return {@code true}, если игрок уже подключался к комнате
     */
    private boolean isReconnect(String gameCode, String playerName) {
        return playerOrderService.contains(gameCode, playerName);
    }
    private void attachPlayerName(WebSocketSession session, String playerName) {
        session.getAttributes().put("PLAYER_NAME", playerName);
    }

    /**
     * Обрабатывает реконнект игрока.
     * <p>
     * Обновляет сессию, восстанавливает состояние игры для комнаты
     * и вызывает сервис жизненного цикла игрока.
     *
     * @param ctx        контекст WebSocket
     * @param session    новая WebSocket-сессия
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     */
    private void handleReconnect(WebSocketContext ctx, WebSocketSession session, String gameCode, String playerName) {
        sessionRegistry.replaceSession(gameCode, playerName, session);

        sendUnitsState(gameCode);
        playerLifecycleService.handleReconnect(ctx);

        log.info("Игрок {} переподключился к комнате {}", playerName, gameCode);
    }

    /**
     * Обрабатывает новый вход игрока в комнату.
     * <p>
     * Регистрирует игрока, создаёт его юнит, отправляет текущее состояние всем
     * игрокам и уведомляет сервис жизненного цикла.
     *
     * @param ctx        контекст WebSocket
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     */
    private void handleNewJoin(WebSocketContext ctx, String gameCode, String playerName) {
        playerOrderService.addPlayer(gameCode, playerName);

        Player playerEntity = playerService.findByUsername(playerName);
        if (playerEntity != null && playerEntity.getActiveUnit() != null) {
            unitRegistry.registerUnit(gameCode, playerName, playerEntity.getActiveUnit());
            log.info("Юнит {} зарегистрирован для игрока {} в комнате {}",
                    playerEntity.getActiveUnit().getName(), playerName, gameCode);
        }

        sendUnitsState(gameCode);
        playerLifecycleService.handleJoin(ctx);
    }



    /**
     * Удаляет игрока из комнаты.
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.removeSession(ctx.gameCode(), session);

        // Обновлённый вызов
        playerOrderService.removePlayer(ctx.gameCode(), ctx.playerName());

        playerLifecycleService.handleLeave(ctx);
    }

    private void sendUnitsState(String gameCode) {
        Set<WebSocketSession> sessions = sessionRegistry.getSessions(gameCode);

        // Получаем список игроков в порядке входа (добавьте такой список в DuelRoomService)
        List<String> playerOrder = playerOrderService.getOrder(gameCode); // например, ["Alice", "Bob"]

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
