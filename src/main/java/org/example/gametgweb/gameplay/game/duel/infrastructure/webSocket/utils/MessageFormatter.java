package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.ChatMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.InfoMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.JoinLeaveMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code MessageFormatter} — сервис для формирования и сериализации сообщений WebSocket в JSON.
 *
 * <p>Назначение:
 * <ul>
 *     <li>формирование стандартизированных JSON-сообщений для разных типов событий в игре;</li>
 *     <li>гарантия корректной сериализации объектов с помощью {@link ObjectMapper};</li>
 *     <li>исключение ручного составления JSON на фронтенде и уменьшение ошибок парсинга.</li>
 * </ul>
 *
 * <p>Общий формат возвращаемых сообщений:
 *
 * <pre>
 * {
 *   "type": "join" | "leave" | "chat" | "info",
 *   "playerName": "&lt;имя игрока&gt;",     // присутствует не во всех сообщениях
 *   "gameCode": "&lt;код комнаты&gt;",     // присутствует в join/leave
 *   "message": "&lt;человеко-читаемый текст&gt;"
 * }
 * </pre>
 *
 * <p>Все методы возвращают уже сериализованную JSON-строку, готовую к отправке через WebSocket.
 */
@Component
public class MessageFormatter {

    private final ObjectMapper mapper;

    /**
     * Создает форматтер сообщений, используя {@link ObjectMapper} настроенный Spring.
     *
     * @param mapper объект Jackson ObjectMapper для сериализации объектов
     */
    @Autowired
    public MessageFormatter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Формирует сообщение о подключении игрока к комнате.
     *
     * @param playerName имя игрока
     * @param gameCode код игровой комнаты
     * @return JSON-строка с типом "join"
     *
     * <p>Пример результата:
     * <pre>
     * {
     *   "type": "join",
     *   "playerName": "Alice",
     *   "gameCode": "ROOM123",
     *   "message": "Alice подключился к комнате ROOM123!"
     * }
     * </pre>
     */
    public String joinMessage(String playerName, String gameCode) {
        JoinLeaveMessageDTO dto = new JoinLeaveMessageDTO(
                "join",
                playerName,
                gameCode,
                playerName + " подключился к комнате " + gameCode + "!"
        );
        return format(dto);
    }

    /**
     * Формирует сообщение о выходе игрока из комнаты.
     *
     * @param playerName имя игрока
     * @param gameCode код игровой комнаты
     * @return JSON-строка с типом "leave"
     *
     * <p>Пример результата:
     * <pre>
     * {
     *   "type": "leave",
     *   "playerName": "Bob",
     *   "gameCode": "ROOM123",
     *   "message": "Bob вышел из комнаты ROOM123!"
     * }
     * </pre>
     */
    public String leaveMessage(String playerName, String gameCode) {
        JoinLeaveMessageDTO dto = new JoinLeaveMessageDTO(
                "leave",
                playerName,
                gameCode,
                playerName + " вышел из комнаты " + gameCode + "!"
        );
        return format(dto);
    }

    /**
     * Формирует сообщение чата от игрока.
     *
     * @param playerName имя отправителя
     * @param message текст сообщения
     * @return JSON-строка с типом "chat"
     *
     * <p>Пример результата:
     * <pre>
     * {
     *   "type": "chat",
     *   "playerName": "Alice",
     *   "message": "Привет!"
     * }
     * </pre>
     */
    public String chatMessage(String playerName, String message) {
        ChatMessageDTO dto = new ChatMessageDTO(playerName, message);
        return format(dto);
    }

    /**
     * Формирует информационное сообщение для игрока (например, при восстановлении соединения).
     *
     * @param playerName имя игрока
     * @return JSON-строка с типом "info"
     *
     * <p>Пример результата:
     * <pre>
     * {
     *   "type": "info",
     *   "message": "Соединение восстановлено для игрока Alice"
     * }
     * </pre>
     */
    public String reconnectMessage(String playerName) {
        InfoMessageDTO dto = new InfoMessageDTO(
                "reconnect",
                "Соединение восстановлено для игрока " + playerName
        );
        return format(dto);
    }

    /**
     * Сериализует любой объект в JSON-строку.
     *
     * @param payload объект для сериализации
     * @return JSON-строка
     * @throws RuntimeException в случае ошибки сериализации
     */
    public String format(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации payload", e);
        }
    }
}