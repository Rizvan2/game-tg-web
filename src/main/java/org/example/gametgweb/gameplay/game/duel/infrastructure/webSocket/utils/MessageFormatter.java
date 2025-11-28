package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.ChatMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.InfoMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.JoinLeaveMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@code MessageFormatter} — сервис сериализации сообщений WebSocket в корректный JSON.
 *
 * <p>Назначение:
 * <ul>
 *     <li>формировать стандартизированные JSON-сообщения для разных WebSocket-событий;</li>
 *     <li>гарантировать корректную сериализацию любых строк, включая вложенные JSON через Jackson;</li>
 *     <li>исключить ручную сборку JSON и ошибки парсинга на фронтенде.</li>
 * </ul>
 *
 * <p>Общий формат возвращаемых сообщений:
 *
 * <pre>
 * {
 *   "type": "join" | "leave" | "chat",
 *   "playerName": "<имя игрока>",
 *   "gameCode": "<код комнаты>",      // есть не везде
 *   "message": "<человеко-читаемый текст>"
 * }
 * </pre>
 *
 * <p>Все методы возвращают JSON-строку, полностью готовую к отправке через WebSocket.
 */
@Component
public class MessageFormatter {

    private final ObjectMapper mapper;

    /**
     * Создаёт форматтер сообщений, используя общий ObjectMapper приложения.
     *
     * @param mapper настроенный Spring'ом {@link ObjectMapper}
     */
    @Autowired
    public MessageFormatter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Формирует JSON-сообщение о присоединении игрока к комнате.
     *
     * @param playerName имя подключившегося игрока
     * @param gameCode   код игровой комнаты
     * @return JSON-строка с полями: type="join", playerName, gameCode, message
     */
    public String joinMessage(String playerName, String gameCode) {
        JoinLeaveMessageDTO dto = new JoinLeaveMessageDTO("join", playerName, gameCode, playerName + " подключился к комнате " + gameCode + "!");
        return format(dto);
    }
    /**
     * Формирует JSON-сообщение о выходе игрока из игровой комнаты.
     *
     * @param playerName имя вышедшего игрока
     * @param gameCode   код игровой комнаты
     * @return JSON-строка с полями: type="leave", playerName, gameCode, message
     */
    public String leaveMessage(String playerName, String gameCode) {
        JoinLeaveMessageDTO dto = new JoinLeaveMessageDTO("leave", playerName, gameCode, playerName + " вышел из комнаты " + gameCode + "!");
        return format(dto);
    }

    public String chatMessage(String playerName, String message) {
        ChatMessageDTO dto = new ChatMessageDTO(playerName, message);
        return format(dto);
    }

    public String reconnectMessage(String playerName) {
        InfoMessageDTO dto = new InfoMessageDTO("reconnect", "Соединение восстановлено для игрока " + playerName);
        return format(dto);
    }

    public String format(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации payload", e);
        }
    }

}