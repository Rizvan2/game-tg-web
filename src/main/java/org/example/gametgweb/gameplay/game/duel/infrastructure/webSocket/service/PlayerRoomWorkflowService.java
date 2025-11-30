package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.SessionRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.UnitStateBroadcaster;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class PlayerRoomWorkflowService {

    private final SessionRegistryService sessionRegistry;
    private final PlayerOrderService playerOrderService;
    private final UnitInitializationService unitInit;
    private final UnitStateBroadcaster broadcaster;
    private final PlayerLifecycleService lifecycle;

    @Autowired
    public PlayerRoomWorkflowService(
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

    public void onNewJoin(WebSocketContext ctx, WebSocketSession session) {
        String game = ctx.gameCode();
        String player = ctx.playerName();

        sessionRegistry.registerNewSession(game, player, session);
        playerOrderService.addPlayer(game, player);
        unitInit.handleNewJoin(game, player);

        broadcaster.broadcastUnitsState(game, playerOrderService.getOrder(game));
        lifecycle.handleJoin(ctx);
    }

    public void onReconnect(WebSocketContext ctx, WebSocketSession session) {
        String game = ctx.gameCode();
        String player = ctx.playerName();

        sessionRegistry.replacePlayerSession(game, player, session);
        playerOrderService.markOnline(game, player);

        broadcaster.broadcastUnitsState(game, playerOrderService.getOrder(game));
        lifecycle.handleReconnect(ctx);
    }

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
