package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

/**
 * DTO для уведомления об уничтожении части тела.
 *
 * @param type       тип уведомления (всегда "BODY_PART_DESTROYED")
 * @param playerUnitName     имя юнита, потерявшего часть тела
 * @param bodyPart   название уничтоженной части тела
 * @param message    текстовое описание события
 */
public record BodyPartDestroyedNotificationDto(
        String type,
        String playerUnitName,
        Body bodyPart,
        String message
) {
    /**
     * Создаёт уведомление об уничтожении части тела.
     *
     * @param playerUnitName имя юнита
     * @param bodyPart   уничтоженная часть тела
     * @param message    сообщение о событии
     * @return готовое DTO для отправки на фронт
     */
    public static BodyPartDestroyedNotificationDto of(String playerUnitName, Body bodyPart, String message) {
        return new BodyPartDestroyedNotificationDto(
                "BODY_PART_DESTROYED",
                playerUnitName,
                bodyPart,
                message
        );
    }
}