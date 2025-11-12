package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.JoinLeaveScheduler;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.MessageFormatter;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.WebSocketContext;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaUnitRepository;
import org.example.gametgweb.gameplay.game.duel.application.services.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * DuelRoomService — сервис управления комнатами дуэлей и игроками в них.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>Регистрацию и удаление WebSocket-сессий игроков;</li>
 *     <li>Управление юнитами игроков в комнате;</li>
 *     <li>Отправку сообщений конкретным игрокам и всем игрокам комнаты;</li>
 *     <li>Логику присоединения и выхода через {@link JoinLeaveScheduler};</li>
 *     <li>Форматирование сообщений через {@link MessageFormatter}.</li>
 * </ul>
 */
@Component
@Slf4j
public class DuelRoomService {

    private final RoomSessionRegistry registry;
    private final JoinLeaveScheduler scheduler;
    private final MessageFormatter formatter;
    private final PlayerServiceImpl playerService;
    private final JpaUnitRepository jpaUnitRepository;

    @Autowired
    public DuelRoomService(RoomSessionRegistry registry, JoinLeaveScheduler scheduler,
                           MessageFormatter formatter, PlayerServiceImpl playerService,
                           JpaUnitRepository jpaUnitRepository) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.formatter = formatter;
        this.playerService = playerService;
        this.jpaUnitRepository = jpaUnitRepository;
    }

    /**
     * Добавляет игрока в комнату дуэли.
     *
     * <p>Метод:
     * <ul>
     *     <li>Регистрирует WebSocket-сессию в {@link RoomSessionRegistry};</li>
     *     <li>Находит юнита игрока и регистрирует его в комнате;</li>
     *     <li>Вызывает {@link JoinLeaveScheduler#handleJoin(WebSocketContext, Runnable)} для отложенной рассылки сообщения о входе;</li>
     *     <li>Логирует подключение игрока.</li>
     * </ul>
     *
     * @param ctx     контекст WebSocket (содержит имя игрока и код комнаты)
     * @param session WebSocket-сессия игрока
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        registry.addSession(ctx.gameCode(), session);

        var playerEntity = playerService.findByUsername(ctx.playerName());
        if (playerEntity != null) {
            var unitOpt = jpaUnitRepository.findByName(playerEntity.getUsername());
            unitOpt.ifPresent(unit -> registry.registerUnit(ctx.gameCode(), ctx.playerName(), unit));
        }

        if (scheduler.handleJoin(ctx, () ->
                registry.broadcast(ctx.gameCode(), formatter.joinMessage(ctx.playerName(), ctx.gameCode())))) {
            log.info("{} подключился к комнате {}", ctx.playerName(), ctx.gameCode());
        }
    }

    /**
     * Удаляет игрока из комнаты.
     *
     * <p>Метод:
     * <ul>
     *     <li>Удаляет WebSocket-сессию из {@link RoomSessionRegistry};</li>
     *     <li>Вызывает {@link JoinLeaveScheduler#handleLeave(WebSocketContext, Runnable)} для отложенной рассылки сообщения о выходе;</li>
     *     <li>Логирует выход игрока.</li>
     * </ul>
     *
     * @param ctx     контекст WebSocket (содержит имя игрока и код комнаты)
     * @param session WebSocket-сессия игрока
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        registry.removeSession(ctx.gameCode(), session);
        scheduler.handleLeave(ctx, () ->
                registry.broadcast(ctx.gameCode(), formatter.leaveMessage(ctx.playerName(), ctx.gameCode())));
        log.info("{} вышел из комнаты {}", ctx.playerName(), ctx.gameCode());
    }

    /**
     * Рассылает чат-сообщение всем игрокам в комнате.
     *
     * @param ctx  контекст WebSocket отправителя
     * @param text текст сообщения
     */
    public void broadcastChat(WebSocketContext ctx, String text) {
        String formatted = formatter.chatMessage(ctx.playerName(), text);
        registry.broadcast(ctx.gameCode(), formatted);
    }

    /**
     * Отправляет сообщение конкретному игроку в комнате.
     *
     * @param gameCode код комнаты
     * @param player   имя игрока
     * @param message  текст сообщения
     */
    public void sendToPlayer(String gameCode, String player, String message) {
        registry.sendToPlayer(gameCode, player, message);
    }

    /**
     * Рассылает сообщение всем игрокам в комнате.
     *
     * @param gameCode код комнаты
     * @param message  текст сообщения
     */
    public void broadcast(String gameCode, String message) {
        registry.broadcast(gameCode, message);
    }
}
