package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Сервис верхнего уровня, отвечающий за доставку WebSocket-сообщений игрокам.
 *
 * <p>Инкапсулирует два ключевых аспекта:
 * <ul>
 *     <li>Поиск активных WebSocket-сессий через {@link RoomSessionRegistry};</li>
 *     <li>Формирование корректного JSON-формата сообщений через {@link MessageFormatter}.</li>
 * </ul>
 *
 * <p>Сервис используется игровыми компонентами для отправки системных событий
 * (подключение, выход, реконнект), игровых данных и чатов. Логика доставки
 * скрыта за единым API, чтобы избежать дублирования кода отправки сообщений.
 *
 * <p>Все методы гарантированно формируют JSON и отправляют его в одну или несколько
 * WebSocket-сессий. Ошибки записи в WebSocket-канал конвертируются в непроверяемые
 * исключения, так как считаются фатальными на уровне игрового цикла.
 */
@Component
public class MessageDispatcherService {

    private final RoomSessionRegistry registry;
    private final MessageFormatter formatter;

    /**
     * Создаёт сервис отправки сообщений.
     *
     * @param registry  реестр активных сессий по комнатам;
     * @param formatter утилита сериализации объектов в JSON WebSocket-сообщения.
     */
    @Autowired
    public MessageDispatcherService(RoomSessionRegistry registry, MessageFormatter formatter) {
        this.registry = registry;
        this.formatter = formatter;
    }

    /**
     * Отправляет текстовое чат-сообщение всем игрокам в комнате.
     *
     * @param gameCode   код игровой комнаты;
     * @param playerName имя отправителя;
     * @param text       текст сообщения.
     */
    public void broadcastChat(String gameCode, String playerName, String text) {
        registry.broadcast(gameCode, formatter.chatMessage(playerName, text));
    }

    /**
     * Рассылает уведомление о подключении нового игрока в комнату.
     *
     * @param gameCode   код комнаты;
     * @param playerName имя подключившегося игрока.
     */
    public void broadcastJoin(String gameCode, String playerName) {
        registry.broadcast(gameCode, formatter.joinMessage(playerName, gameCode));
    }

    /**
     * Рассылает уведомление о выходе игрока из комнаты.
     *
     * @param gameCode   код комнаты;
     * @param playerName имя вышедшего игрока.
     */
    public void broadcastLeave(String gameCode, String playerName) {
        registry.broadcast(gameCode, formatter.leaveMessage(playerName, gameCode));
    }

    /**
     * Рассылает системное сообщение о реконнекте игрока.
     * Используется для восстановления состояния при переподключении.
     *
     * @param gameCode   код комнаты;
     * @param playerName имя игрока, переподключившегося к комнате.
     */
    public void broadcastReconnect(String gameCode, String playerName) {
        registry.broadcast(gameCode, formatter.reconnectMessage(playerName));
    }

    /**
     * Отправляет сообщение конкретному игроку по имени.
     *
     * <p>Если игрок не имеет активной WebSocket-сессии,
     * метод молча игнорирует отправку (поведение определено в registry).
     *
     * @param gameCode   код комнаты;
     * @param playerName имя получателя;
     * @param message    готовая строка JSON-сообщения.
     */
    public void sendToPlayer(String gameCode, String playerName, String message) {
        registry.sendToPlayer(gameCode, playerName, message);
    }

    /**
     * Отправляет объект в конкретную WebSocket-сессию.
     * Объект автоматически сериализуется в JSON через {@link MessageFormatter}.
     *
     * @param session целевая WebSocket-сессия;
     * @param payload произвольный объект, который должен быть отправлен.
     * @throws RuntimeException в случае ошибки записи в WebSocket-канал.
     */
    public void send(WebSocketSession session, Object payload) {
        String message = formatter.format(payload);
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
