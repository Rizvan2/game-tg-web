package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.application.services.GameSessionService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.JoinLeaveScheduler;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.springframework.stereotype.Component;

/**
 * Сервис обработки жизненного цикла игрока в комнате.
 *
 * <p>Отвечает за реакцию системы на подключение, выход и переподключение игрока.
 * Делегирует отложенную отправку событий подключений/выходов в {@link JoinLeaveScheduler},
 * а широковещательную отправку сообщений — в {@link MessageDispatcherService}.
 *
 * <p>Основная цель — централизованно управлять игровые события, связанные с
 * появлением и исчезновением игроков в WebSocket-комнатах.
 */
@Component
@Slf4j
public class PlayerLifecycleService {

    private final JoinLeaveScheduler scheduler;
    private final MessageDispatcherService dispatcherService;
    private final GameSessionService gameSessionService;

    /**
     * Создаёт экземпляр сервиса жизненного цикла игрока.
     *
     * @param scheduler          Планировщик для отложенной обработки входов/выходов.
     * @param dispatcherService  Сервис для широковещательной рассылки игровых сообщений.
     */
    public PlayerLifecycleService(JoinLeaveScheduler scheduler, MessageDispatcherService dispatcherService, GameSessionService gameSessionService) {
        this.scheduler = scheduler;
        this.dispatcherService = dispatcherService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Обрабатывает событие нового подключения игрока.
     *
     * <p>Передаёт планировщику задачу отправить широковещательное сообщение о присоединении,
     * с учётом возможной задержки (анти-фликер логики). Если подключение подтверждено —
     * логгирует событие.
     *
     * @param ctx контекст WebSocket — включает gameCode и playerName
     */
    public void handleJoin(WebSocketContext ctx) {
        if (scheduler.handleJoin(ctx, () ->
                dispatcherService.broadcastJoin(ctx.gameCode(), ctx.playerName()))) {
            log.info("{} подключился к комнате {}", ctx.playerName(), ctx.gameCode());
        }
    }

    /**
     * Обрабатывает событие выхода игрока из комнаты.
     *
     * <p>Передаёт планировщику задачу на обработку выхода и рассылки сообщения,
     * затем логгирует факт выхода.
     *
     * @param ctx контекст WebSocket — включает gameCode и playerName
     */
    public void handleLeave(WebSocketContext ctx) {
        scheduler.handleLeave(ctx, () -> {
            gameSessionService.removePlayerFromGame(ctx.gameCode(), ctx.playerName());
            dispatcherService.broadcastLeave(ctx.gameCode(), ctx.playerName());
        });

        log.info("{} вышел из комнаты {}", ctx.playerName(), ctx.gameCode());
    }

    /**
     * Обрабатывает переподключение игрока.
     *
     * <p>Сразу инициирует рассылку уведомления о реконнекте без задержек,
     * затем пишет в лог соответствующее событие.
     *
     * @param ctx контекст WebSocket — включает gameCode и playerName
     */
    public void handleReconnect(WebSocketContext ctx) {
        dispatcherService.broadcastReconnect(ctx.gameCode(), ctx.playerName());
        log.info("{} восстановил соединение в комнате {}", ctx.playerName(), ctx.gameCode());
    }
}
