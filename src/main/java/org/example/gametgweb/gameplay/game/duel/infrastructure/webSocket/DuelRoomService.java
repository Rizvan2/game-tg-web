package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.PlayerRoomWorkflowService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.UnitStateBroadcaster;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;


@Component
public class DuelRoomService {

    private final PlayerRoomWorkflowService workflow;
    private final PlayerOrderService playerOrder;

    @Autowired
    public DuelRoomService(PlayerRoomWorkflowService workflow,
                           PlayerOrderService playerOrder) {
        this.workflow = workflow;
        this.playerOrder = playerOrder;
    }

    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        if (isReconnect(ctx)) {
            workflow.onReconnect(ctx, session);
        } else {
            workflow.onNewJoin(ctx, session);
        }
    }

    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        workflow.onLeave(ctx, session);
    }

    private boolean isReconnect(WebSocketContext ctx) {
        return playerOrder.contains(ctx.gameCode(), ctx.playerName())
                && playerOrder.isOffline(ctx.gameCode(), ctx.playerName());
    }
    /**
     * Отправляет текущее состояние всех юнитов в указанной комнате всем активным сессиям.
     *
     * <p>Использует {@link UnitStateBroadcaster} и порядок игроков из {@link PlayerOrderService}.
     *
     * @param gameCode Код комнаты дуэли.
     */
    public void sendUnitsState(String gameCode) {
        workflow.sendUnitsState(gameCode);
    }
}