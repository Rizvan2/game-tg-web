package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.SessionRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.UnitStateBroadcaster;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * DuelRoomCoordinator — Высокоуровневый сервис-координатор, управляющий логикой присутствия
 * игроков и их жизненным циклом в комнате дуэли.
 *
 * <p>Класс реализует Принцип единственной ответственности (SRP), выступая в роли диспетчера.
 * Он координирует работу специализированных сервисов ({@link SessionRegistryService},
 * {@link PlayerOrderService}, {@link UnitInitializationService}, {@link PlayerLifecycleService})
 * для обработки трех ключевых состояний игрока:</p>
 * <ul>
 * <li><strong>Первичное подключение (onNewJoin)</strong> — регистрация сессии и инициализация юнита.</li>
 * <li><strong>Переподключение (onReconnect)</strong> — восстановление сессии и обновление статуса.</li>
 * <li><strong>Отключение (onLeave)</strong> — корректное удаление сессии и запуск таймера выхода.</li>
 * </ul>
 */
@Component
public class DuelRoomCoordinator {

    private final SessionRegistryService sessionRegistry;
    private final PlayerOrderService playerOrderService;
    private final UnitInitializationService unitInit;
    private final UnitStateBroadcaster broadcaster;
    private final PlayerLifecycleService lifecycle;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param sessionRegistry    Сервис для управления активными WebSocket-сессиями.
     * @param playerOrderService Сервис для управления порядком игроков и их статусом (онлайн/оффлайн).
     * @param unitInit           Сервис для загрузки данных игрока и регистрации его активного юнита.
     * @param broadcaster        Сервис для широковещательной рассылки состояния юнитов.
     * @param lifecycle          Сервис для обработки событий жизненного цикла игрока (присоединение, переподключение, выход).
     */
    @Autowired
    public DuelRoomCoordinator(
            SessionRegistryService sessionRegistry,
            PlayerOrderService playerOrderService,
            UnitInitializationService unitInit,
            UnitStateBroadcaster broadcaster,
            PlayerLifecycleService lifecycle
    ) {
        this.sessionRegistry = sessionRegistry;
        this.playerOrderService = playerOrderService;
        this.unitInit = unitInit;
        this.broadcaster = broadcaster;
        this.lifecycle = lifecycle;
    }

    /**
     * Обрабатывает логику первого присоединения нового игрока к комнате.
     *
     * <p>Выполняет последовательность действий:
     * <ol>
     * <li>Регистрирует новую WebSocket-сессию.</li>
     * <li>Добавляет игрока в порядок комнаты.</li>
     * <li>Инициализирует и регистрирует активный юнит игрока.</li>
     * <li>Рассылает обновленное состояние юнитов всем игрокам.</li>
     * <li>Инициирует обработку события присоединения (через {@link PlayerLifecycleService}).</li>
     * </ol>
     *
     * @param ctx     Контекст WebSocket-сообщения (gameCode, playerName).
     * @param session WebSocket-сессия игрока.
     */
    public void onNewJoin(WebSocketContext ctx, WebSocketSession session) {
        String game = ctx.gameCode();
        String player = ctx.playerName();

        sessionRegistry.registerNewSession(game, player, session);
        playerOrderService.addPlayer(game, player);
        unitInit.handleNewJoin(game, player);

        broadcaster.broadcastUnitsState(game, playerOrderService.getOrder(game));
        lifecycle.handleJoin(ctx);
    }

    /**
     * Обрабатывает логику переподключения (реконнекта) существующего игрока.
     *
     * <p>Выполняет последовательность действий:
     * <ol>
     * <li>Заменяет старую WebSocket-сессию новой.</li>
     * <li>Помечает игрока как онлайн (снимает статус оффлайн).</li>
     * <li>Рассылает обновленное состояние юнитов всем игрокам.</li>
     * <li>Инициирует обработку события переподключения (через {@link PlayerLifecycleService}).</li>
     * </ol>
     *
     * @param ctx     Контекст WebSocket-сообщения (gameCode, playerName).
     * @param session Новая WebSocket-сессия игрока.
     */
    public void onReconnect(WebSocketContext ctx, WebSocketSession session) {
        String game = ctx.gameCode();
        String player = ctx.playerName();

        sessionRegistry.replacePlayerSession(game, player, session);
        playerOrderService.markOnline(game, player);

        broadcaster.broadcastUnitsState(game, playerOrderService.getOrder(game));
        lifecycle.handleReconnect(ctx);
    }

    /**
     * Обрабатывает событие выхода (отключения) игрока из комнаты.
     *
     * <p>Выполняет последовательность действий:
     * <ol>
     * <li>Удаляет WebSocket-сессию из реестра.</li>
     * <li>Помечает игрока как оффлайн и запускает таймер удаления (через {@link PlayerOrderService}).</li>
     * <li>Инициирует обработку события выхода (через {@link PlayerLifecycleService}).</li>
     * </ol>
     *
     * @param ctx     Контекст WebSocket-сообщения (gameCode, playerName).
     * @param session WebSocket-сессия, которую необходимо удалить.
     */
    public void onLeave(WebSocketContext ctx, WebSocketSession session) {
        String game = ctx.gameCode();
        String player = ctx.playerName();

        sessionRegistry.removeSession(game, session);
        playerOrderService.removePlayer(game, player);

        lifecycle.handleLeave(ctx);
    }

    /**
     * Отправляет текущее состояние всех юнитов в указанной комнате всем активным сессиям.
     *
     * <p>Использует {@link UnitStateBroadcaster} и порядок игроков из {@link PlayerOrderService}.
     *
     * @param gameCode Код комнаты дуэли.
     */
    public void sendUnitsState(String gameCode) {
        broadcaster.broadcastUnitsState(gameCode, playerOrderService.getOrder(gameCode));
    }
}
