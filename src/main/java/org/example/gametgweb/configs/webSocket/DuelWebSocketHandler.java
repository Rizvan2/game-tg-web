package org.example.gametgweb.configs.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * {@code DuelWebSocketHandler} — обработчик WebSocket-соединений между игроками.
 * <p>
 * Отвечает за жизненный цикл соединений:
 * <ul>
 *     <li>Добавление и регистрацию нового подключения;</li>
 *     <li>Отложенную обработку выхода/входа игрока (через {@link JoinLeaveScheduler});</li>
 *     <li>Рассылку системных сообщений о присоединении и выходе;</li>
 *     <li>Закрытие некорректных сессий с отсутствующим параметром {@code gameCode}.</li>
 * </ul>
 * <p>
 * Все активные соединения хранятся в {@link RoomSessionRegistry}, что обеспечивает
 * потокобезопасную работу с множеством одновременно подключённых клиентов.
 */
@Slf4j
@Component
public class DuelWebSocketHandler extends TextWebSocketHandler {

    /** Реестр активных WebSocket-сессий, сгруппированных по игровым комнатам. */
    private final RoomSessionRegistry registry;

    /** Планировщик отложенных событий подключения и выхода игроков. */
    private final JoinLeaveScheduler scheduler;

    /** Форматирует системные сообщения (например, "Игрок подключился" или "Игрок покинул комнату"). */
    private final MessageFormatter formatter;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param registry  менеджер активных сессий по игровым кодам
     * @param scheduler планировщик отложенных действий при входе/выходе
     * @param formatter утилита для форматирования текстовых уведомлений
     */
    @Autowired
    public DuelWebSocketHandler(RoomSessionRegistry registry,
                                JoinLeaveScheduler scheduler,
                                MessageFormatter formatter) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.formatter = formatter;
    }

    /**
     * Вызывается при успешном установлении нового WebSocket-соединения.
     * <p>
     * Извлекает контекст соединения ({@link WebSocketContext}) из параметров запроса.
     * Если контекст отсутствует или параметр {@code gameCode} не найден — соединение закрывается.
     * В противном случае игрок добавляется в комнату, и выполняется отложенная логика входа.
     *
     * @param session активная WebSocket-сессия
     * @throws Exception при ошибке инициализации соединения
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) {
            closeBadSession(session);
            return;
        }
        handleJoin(ctx, session);
    }

    /**
     * Вызывается при закрытии WebSocket-соединения.
     * <p>
     * Удаляет игрока из комнаты и запускает отложенную проверку:
     * если через установленный интервал игрок не переподключится, рассылается сообщение о выходе.
     *
     * @param session закрытая сессия
     * @param status  причина и код закрытия соединения
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) return;

        registry.removeSession(ctx.gameCode(), session);
        scheduler.handleLeave(ctx, () ->
                registry.broadcast(ctx.gameCode(), formatter.leaveMessage(ctx.playerName(), ctx.gameCode())));

        log.info("WebSocket закрыт: {}, статус: {}", session.getId(), status);
    }

    /**
     * Обрабатывает входящие текстовые сообщения от WebSocket-сессии.
     * <p>
     * Метод выполняет следующие действия:
     * <ol>
     *     <li>Извлекает контекст соединения {@link WebSocketContext} из сессии, содержащий имя игрока и код комнаты.</li>
     *     <li>Если контекст отсутствует, сообщение игнорируется.</li>
     *     <li>Парсит текстовое сообщение в JSON с помощью {@link ObjectMapper}.</li>
     *     <li>Определяет тип сообщения по полю "type".</li>
     *     <li>Если тип сообщения равен "chat":</li>
     *     <ul>
     *         <li>Извлекает текст сообщения из поля "message".</li>
     *         <li>Форматирует сообщение через {@link MessageFormatter} в вид "[playerName]: message".</li>
     *         <li>Рассылает сформированное сообщение всем игрокам в комнате через {@link RoomSessionRegistry#broadcast(String, String)}.</li>
     *     </ul>
     * </ol>
     *
     * @param session активная WebSocket-сессия, от которой пришло сообщение
     * @param message текстовое сообщение от клиента
     * @throws Exception если произошла ошибка парсинга JSON или отправки сообщения
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        var ctx = WebSocketContext.from(session);
        if (ctx == null) return;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode payload = mapper.readTree(message.getPayload());
        String type = payload.has("type") ? payload.get("type").asText() : null;

        if ("chat".equals(type)) {
            String text = payload.has("message") ? payload.get("message").asText() : "";
            String player = ctx.playerName();

            String formatted = formatter.chatMessage(player, text);
            registry.broadcast(ctx.gameCode(), formatted);
        }
    }


    /**
     * Закрывает соединение с ошибкой, если отсутствует обязательный параметр {@code gameCode}.
     *
     * @param session сессия, подлежащая закрытию
     * @throws IOException при сбое закрытия соединения
     */
    private void closeBadSession(WebSocketSession session) throws IOException {
        session.close(CloseStatus.BAD_DATA.withReason("Missing gameCode parameter"));
    }

    /**
     * Обрабатывает успешное подключение игрока:
     * <ul>
     *     <li>добавляет игрока в комнату;</li>
     *     <li>инициирует отложенное событие «входа»;</li>
     *     <li>рассылает уведомление о присоединении при подтверждённом подключении.</li>
     * </ul>
     *
     * @param ctx     контекст WebSocket-подключения (содержит код комнаты и имя игрока)
     * @param session активная сессия игрока
     */
    private void handleJoin(WebSocketContext ctx, WebSocketSession session) {
        registry.addSession(ctx.gameCode(), session);
        if (scheduler.handleJoin(ctx, () ->
                registry.broadcast(ctx.gameCode(), formatter.joinMessage(ctx.playerName(), ctx.gameCode())))) {
            logJoin(ctx);
        }
    }

    /**
     * Логирует факт подключения игрока к комнате.
     *
     * @param ctx контекст с именем игрока и кодом комнаты
     */
    private void logJoin(WebSocketContext ctx) {
        log.info("{} подключился к комнате {}", ctx.playerName(), ctx.gameCode());
    }
}
