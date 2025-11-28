package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.JoinLeaveScheduler;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlayerLifecycleService {
    private final JoinLeaveScheduler scheduler;
    private final MessageDispatcherService dispatcherService;


    public PlayerLifecycleService(JoinLeaveScheduler scheduler, MessageDispatcherService dispatcherService) {
        this.scheduler = scheduler;
        this.dispatcherService = dispatcherService;
    }

    public void handleJoin(WebSocketContext ctx) {
        // Делегируем рассылку сообщений в MessageDispatcherService
        if (scheduler.handleJoin(ctx, () ->
                dispatcherService.broadcastJoin(ctx.gameCode(), ctx.playerName()))) {
            log.info("{} подключился к комнате {}", ctx.playerName(), ctx.gameCode());
        }
    }

    public void handleLeave(WebSocketContext ctx) {
        scheduler.handleLeave(ctx, () ->
                dispatcherService.broadcastLeave(ctx.gameCode(), ctx.playerName()));
        log.info("{} вышел из комнаты {}", ctx.playerName(), ctx.gameCode());
    }

    public void handleReconnect(WebSocketContext ctx) {

        dispatcherService.broadcastReconnect(ctx.gameCode(), ctx.playerName());

        log.info("{} восстановил соединение в комнате {}", ctx.playerName(), ctx.gameCode());
    }
}
