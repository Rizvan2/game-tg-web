package org.example.gametgweb.gameplay.game.duel.application.services.duel;

import org.example.gametgweb.gameplay.game.duel.application.services.order.PlayerOrderService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.UnitStateBroadcaster;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.WebSocketContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Высокоуровневый сервис управления логикой присутствия игроков в комнате дуэли.
 *
 * <p>Отвечает за три ключевых состояния жизненного цикла игрока:
 * <ul>
 *     <li><strong>Первичное подключение</strong> — регистрация игрока и инициализация необходимых данных.</li>
 *     <li><strong>Переподключение</strong> — восстановление состояния после кратковременного обрыва соединения.</li>
 *     <li><strong>Отключение</strong> — корректное удаление игрока и его игровых сущностей.</li>
 * </ul>
 *
 * <p>Сервис делегирует низкоуровневую обработку сервисам нижележащего уровня
 * ({@code DuelRoomCoordinator}) и использует {@code PlayerOrderService}
 * для определения попытки реконнекта.</p>
 *
 * <p>Иначе говоря, это диспетчер комнаты дуэли — строгий, собранный и всегда знает, кто действительно вернулся,
 * а кто просто забыл выключить Wi-Fi.</p>
 */
@Component
public class DuelRoomService {

    private final DuelRoomCoordinator workflow;
    private final PlayerOrderService playerOrder;

    @Autowired
    public DuelRoomService(DuelRoomCoordinator workflow,
                           PlayerOrderService playerOrder) {
        this.workflow = workflow;
        this.playerOrder = playerOrder;
    }

    /**
     * Обрабатывает попытку подключения игрока.
     *
     * <p>Если игрок ранее присутствовал в комнате, но находится в состоянии «оффлайн»,
     * то подключение трактуется как переподключение и обрабатывается через
     * {@link DuelRoomCoordinator#onReconnect(WebSocketContext, WebSocketSession)}.
     *
     * <p>Иначе выполняется стандартная процедура нового подключения:
     * регистрация сессии, добавление игрока в игровой порядок и инициализация юнитов.
     *
     * @param ctx     Контекст комнаты и имени игрока.
     * @param session Текущая WebSocket-сессия.
     */
    public void playerJoin(WebSocketContext ctx, WebSocketSession session) {
        if (isReconnect(ctx)) {
            workflow.onReconnect(ctx, session);
        } else {
            workflow.onNewJoin(ctx, session);
        }
    }

    /**
     * Обрабатывает отключение игрока от комнаты дуэли.
     *
     * <p>Выполняет очистку игровой сессии, исключение игрока из порядка,
     * а также делегирует доменные действия в {@link DuelRoomCoordinator#onLeave(WebSocketContext, WebSocketSession)}.
     *
     * @param ctx     Контекст комнаты и имени игрока.
     * @param session Сессия, покидающая комнату.
     */
    public void playerLeave(WebSocketContext ctx, WebSocketSession session) {
        workflow.onLeave(ctx, session);
    }

    /**
     * Проверяет, является ли текущее подключение игрока попыткой переподключения.
     *
     * <p>Игрок считается переподключающимся, если:
     * <ul>
     *     <li>он присутствует в игровом порядке комнаты;</li>
     *     <li>он помечен как оффлайн.</li>
     * </ul>
     *
     * @param ctx Контекст комнаты дуэли.
     * @return {@code true}, если подключение — реконнект; иначе {@code false}.
     */
    private boolean isReconnect(WebSocketContext ctx) {
        return playerOrder.contains(ctx.gameCode(), ctx.playerName())
                && playerOrder.isOffline(ctx.gameCode(), ctx.playerName());
    }

    /**
     * Отправляет текущее состояние всех игровых юнитов в указанной комнате всем активным сессиям.
     *
     * <p>Использует {@link UnitStateBroadcaster} для отправки, а порядок игроков
     * извлекается из {@link PlayerOrderService}.
     *
     * @param gameCode Код комнаты дуэли.
     */
    public void sendUnitsState(String gameCode) {
        workflow.sendUnitsState(gameCode);
    }
}