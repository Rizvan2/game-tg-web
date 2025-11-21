package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        try {
            return mapper.writeValueAsString(Map.of(
                    "type", "join",
                    "playerName", playerName,
                    "gameCode", gameCode,
                    "message", playerName + " подключился к комнате " + gameCode + "!"
            ));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации joinMessage", e);
        }
    }

    /**
     * Формирует JSON-сообщение о выходе игрока из игровой комнаты.
     *
     * @param playerName имя вышедшего игрока
     * @param gameCode   код игровой комнаты
     * @return JSON-строка с полями: type="leave", playerName, gameCode, message
     */
    public String leaveMessage(String playerName, String gameCode) {
        try {
            return mapper.writeValueAsString(Map.of(
                    "type", "leave",
                    "playerName", playerName,
                    "gameCode", gameCode,
                    "message", playerName + " вышел из комнаты " + gameCode + "!"
            ));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации leaveMessage", e);
        }
    }

    /**
     * Формирует JSON-сообщение чата.
     *
     * <p>Используется как для внутренних игровых сообщений,
     * так и для пользовательского текста.
     *
     * @param playerName имя игрока, отправившего сообщение
     * @param message    текстовое содержимое сообщения
     * @return JSON-строка с полями: type="chat", playerName, message
     */
    public String chatMessage(String playerName, String message) {
        try {
            return mapper.writeValueAsString(Map.of(
                    "type", "chat",
                    "playerName", playerName,
                    "message", message
            ));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации chatMessage", e);
        }
    }

    public String format(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сериализации payload", e);
        }
    }

}