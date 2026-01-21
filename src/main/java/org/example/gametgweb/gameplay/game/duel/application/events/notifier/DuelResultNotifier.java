package org.example.gametgweb.gameplay.game.duel.application.events.notifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.session.RoomSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
/**
 * DuelResultNotifier — сервис уведомлений о результате дуэли.
 *
 * <p>Отвечает за формирование и отправку WebSocket-сообщений игрокам
 * после завершения дуэли.</p>
 *
 * <p>Сервис инкапсулирует:
 * <ul>
 *     <li>структуру отправляемого сообщения;</li>
 *     <li>сериализацию данных в JSON;</li>
 *     <li>взаимодействие с {@link RoomSessionRegistry}.</li>
 * </ul>
 * </p>
 *
 * <p>Формат отправляемого сообщения:
 * <pre>
 * {
 *   "type": "duelResult",
 *   "resultText": "...",
 *   "targetPlayer": "playerName"
 * }
 * </pre>
 * </p>
 *
 * <p>Таким образом, изменения формата сообщений или транспорта
 * локализованы в одном месте и не затрагивают слушатели событий
 * или доменную логику.</p>
 */
@Service
public class DuelResultNotifier {

    private final RoomSessionRegistry roomSessionRegistry;
    private final ObjectMapper objectMapper;

    public DuelResultNotifier(RoomSessionRegistry roomSessionRegistry,
                              ObjectMapper objectMapper) {
        this.roomSessionRegistry = roomSessionRegistry;
        this.objectMapper = objectMapper;
    }
    /**
     * Отправляет игроку уведомление о победе в дуэли.
     *
     * @param gameCode   код игровой сессии
     * @param playerName имя игрока-получателя
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    public void sendWin(String gameCode, String playerName)
            throws JsonProcessingException {

        send(gameCode, playerName, "Вы победили!");
    }

    /**
     * Отправляет игроку уведомление о поражении в дуэли.
     *
     * @param gameCode   код игровой сессии
     * @param playerName имя игрока-получателя
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    public void sendLose(String gameCode, String playerName)
            throws JsonProcessingException {

        send(gameCode, playerName, "Вы проиграли!");
    }

    /**
     * Формирует и отправляет WebSocket-сообщение игроку.
     *
     * <p>Метод является внутренним и не предназначен для прямого использования
     * за пределами сервиса.</p>
     *
     * @param gameCode   код игровой сессии
     * @param playerName имя игрока-получателя
     * @param text       текст результата дуэли
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    private void send(String gameCode,
                      String playerName,
                      String text) throws JsonProcessingException {

        roomSessionRegistry.sendToPlayer(
                gameCode,
                playerName,
                objectMapper.writeValueAsString(Map.of(
                        "type", "duelResult",
                        "resultText", text,
                        "targetPlayer", playerName
                ))
        );
    }
}
