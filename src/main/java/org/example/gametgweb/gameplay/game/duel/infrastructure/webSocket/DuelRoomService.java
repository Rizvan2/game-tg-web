package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.webSocket.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.SessionRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.UnitStateBroadcaster;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

/**
 * DuelRoomService — сервис управления комнатами дуэлей и игроками.
 *
 * <p>Отвечает за:
 * <ul>
 * <li>Регистрацию и удаление WebSocket-сессий игроков;</li>
 * <li>Управление юнитами игроков в комнате;</li>
 * <li>Логику присоединения и выхода через {@link JoinLeaveScheduler}.</li>
 * </ul>
 */
@Component
@Slf4j
public class DuelRoomService {

    private final PlayerRepositoryImpl playerService;
    private final UnitRegistryService unitRegistry;
    private final SessionRegistryService sessionRegistry;
    private final PlayerLifecycleService playerLifecycleService;
    private final PlayerOrderService playerOrderService;
    private final UnitStateBroadcaster unitStateBroadcaster;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param sessionRegistry        Сервис для управления активными WebSocket-сессиями.
     * @param playerService          Репозиторий для получения данных игрока из базы данных.
     * @param unitRegistry           Сервис для регистрации и получения игровых юнитов в контексте игры.
     * @param playerLifecycleService Сервис для обработки событий жизненного цикла игрока (присоединение, переподключение, выход).
     * @param playerOrderService     Сервис для управления порядком игроков и их статусом (онлайн/оффлайн).
     * @param unitStateBroadcaster   Сервис для широковещательной рассылки состояния юнитов.
     */
    @Autowired
    public DuelRoomService(SessionRegistryService sessionRegistry,
                           PlayerRepositoryImpl playerService,
                           UnitRegistryService unitRegistry,
                           PlayerLifecycleService playerLifecycleService, PlayerOrderService playerOrderService, UnitStateBroadcaster unitStateBroadcaster) {
        this.sessionRegistry = sessionRegistry;
        this.playerService = playerService;
        this.unitRegistry = unitRegistry;
        this.playerLifecycleService = playerLifecycleService;
        this.playerOrderService = playerOrderService;
        this.unitStateBroadcaster = unitStateBroadcaster;
    }

    /**
     * Обрабатывает событие присоединения игрока к комнате.
     *
     * <p>Определяет, является ли это переподключением (реконнектом) или новым присоединением,
     * и вызывает соответствующую логику.
     *
     * @param ctx     Контекст WebSocket-сообщения, содержащий gameCode и playerName.
     * @param session WebSocket-сессия игрока.
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        String gameCode = ctx.gameCode();
        String playerName = ctx.playerName();

        if (isReconnect(gameCode, playerName) && playerOrderService.isOffline(gameCode, playerName)) {
            playerOrderService.markOnline(gameCode, playerName);
            handleReconnect(ctx, session, gameCode, playerName);
        } else {
            sessionRegistry.registerNewSession(gameCode, playerName, session);
            handleNewJoin(ctx, gameCode, playerName);
        }
    }

    /**
     * Обрабатывает событие выхода (отключения) игрока из комнаты.
     *
     * <p>Удаляет сессию игрока, обновляет порядок игроков и вызывает обработчик жизненного цикла.
     *
     * @param ctx     Контекст WebSocket-сообщения, содержащий gameCode и playerName.
     * @param session WebSocket-сессия, которую необходимо удалить.
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.removeSession(ctx.gameCode(), session);

        // Обновлённый вызов
        playerOrderService.removePlayer(ctx.gameCode(), ctx.playerName());

        playerLifecycleService.handleLeave(ctx);
    }

    /**
     * Отправляет текущее состояние всех юнитов в указанной комнате всем активным сессиям.
     *
     * <p>Использует {@link UnitStateBroadcaster} и порядок игроков из {@link PlayerOrderService}.
     *
     * @param gameCode Код комнаты дуэли.
     */
    public void sendUnitsState(String gameCode) {
        unitStateBroadcaster.broadcastUnitsState(gameCode, playerOrderService.getOrder(gameCode));
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

    /**
     * Обрабатывает логику переподключения (реконнекта) существующего игрока.
     *
     * <p>Заменяет старую сессию новой, отправляет состояние юнитов и вызывает обработчик жизненного цикла.
     *
     * @param ctx        Контекст WebSocket.
     * @param session    Новая WebSocket-сессия игрока.
     * @param gameCode   Код комнаты.
     * @param playerName Имя игрока.
     */
    private void handleReconnect(WebSocketContext ctx, WebSocketSession session, String gameCode, String playerName) {
        sessionRegistry.replacePlayerSession(gameCode, playerName, session);

        sendUnitsState(gameCode);
        playerLifecycleService.handleReconnect(ctx);

        log.info("Игрок {} переподключился к комнате {}", playerName, gameCode);
    }

    /**
     * Обрабатывает логику нового присоединения игрока к комнате.
     *
     * <p>Регистрирует сессию, добавляет игрока в порядок, регистрирует его активного юнита,
     * отправляет состояние юнитов и вызывает обработчик жизненного цикла.
     *
     * @param ctx        Контекст WebSocket.
     * @param gameCode   Код комнаты.
     * @param playerName Имя игрока.
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
     * Проверяет, является ли попытка присоединения переподключением (реконнектом).
     *
     * @param gameCode   Код комнаты.
     * @param playerName Имя игрока.
     * @return {@code true}, если игрок уже зарегистрирован в порядке игроков комнаты, иначе {@code false}.
     */
    private boolean isReconnect(String gameCode, String playerName) {
        return playerOrderService.contains(gameCode, playerName);
    }
}
