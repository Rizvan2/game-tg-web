package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.BodyPartDestroyedNotificationDto;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.stereotype.Service;

/**
 * BodyPartNotifier — сервис уведомлений о состоянии частей тела юнитов.
 *
 * <p>Отвечает за формирование и отправку WebSocket-сообщений игрокам
 * при изменении состояния частей тела (уничтожение, повреждение и т.д.).</p>
 *
 * <p>Формат отправляемого сообщения:
 * <pre>
 * {
 *   "type": "BODY_PART_DESTROYED",
 *   "player": "playerName",
 *   "bodyPart": "HEAD",
 *   "message": "playerName потерял голову!"
 * }
 * </pre>
 * </p>
 */
@Service
public class BodyPartNotifier {

    private final RoomSessionRegistry roomSessionRegistry;
    private final ObjectMapper objectMapper;

    public BodyPartNotifier(RoomSessionRegistry roomSessionRegistry,
                            ObjectMapper objectMapper) {
        this.roomSessionRegistry = roomSessionRegistry;
        this.objectMapper = objectMapper;
    }

    /**
     * Отправляет всем игрокам в комнате уведомление об уничтожении части тела.
     *
     * @param gameCode   код игровой сессии
     * @param playerName имя игрока, потерявшего часть тела
     * @param bodyPart   уничтоженная часть тела
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    public void sendBodyPartDestroyed(String gameCode, String playerName, Body bodyPart)
            throws JsonProcessingException {

        String bodyPartName = getBodyPartName(bodyPart);
        String message = String.format("%s потерял %s!", playerName, bodyPartName);

        roomSessionRegistry.broadcast(
                gameCode,
                objectMapper
                        .writeValueAsString(BodyPartDestroyedNotificationDto.of(
                                playerName,
                                bodyPart,
                                message)
                        )
        );
    }

    /**
     * Возвращает русское название части тела в винительном падеже.
     *
     * @param bodyPart часть тела
     * @return название в винительном падеже
     */
    private String getBodyPartName(Body bodyPart) {
        return switch (bodyPart) {
            case HEAD -> "голову";
            case CHEST -> "грудь";
            case LEFT_ARM -> "левую руку";
            case RIGHT_ARM -> "правую руку";
            case LEFT_LEG -> "левую ногу";
            case RIGHT_LEG -> "правую ногу";
        };
    }
}