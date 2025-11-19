package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.JoinLeaveScheduler;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.PlayerLifecycleService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.SessionRegistryService;
import org.example.gametgweb.characterSelection.infrastructure.webSocket.UnitRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

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


    @Autowired
    public DuelRoomService(SessionRegistryService sessionRegistry,
                           PlayerRepositoryImpl playerService,
                           UnitRegistryService unitRegistry,
                           PlayerLifecycleService playerLifecycleService) {
        this.sessionRegistry = sessionRegistry;
        this.playerService = playerService;
        this.unitRegistry = unitRegistry;
        this.playerLifecycleService = playerLifecycleService;
    }

    /**
     * Добавляет игрока в комнату дуэли.
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.addSession(ctx.gameCode(), session);

        Player playerEntity = playerService.findByUsername(ctx.playerName());
        if (playerEntity != null) {
            Unit unit = playerEntity.getActiveUnit();
            if (unit != null) {
                unitRegistry.registerUnit(ctx.gameCode(), ctx.playerName(), unit);
                log.info("Юнит {} зарегистрирован для игрока {} в комнате {}", unit.getName(), ctx.playerName(), ctx.gameCode());
            } else {
                log.warn("Не найден юнит для игрока {}", playerEntity.getUsername());
            }
        }

        playerLifecycleService.handleJoin(ctx);
    }

    /**
     * Удаляет игрока из комнаты.
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        sessionRegistry.removeSession(ctx.gameCode(), session);
        playerLifecycleService.handleLeave(ctx);
    }
}
