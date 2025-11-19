package org.example.gametgweb.characterSelection.api.dto;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;

/**
 * DTO для передачи информации о игроке на фронтенд.
 * Используется, например, при выборе персонажа.
 * <p>
 * Содержит идентификатор игрока, его имя и ID активного юнита.
 */
public record PlayerResponse(
        /** Уникальный идентификатор игрока в системе */
        Long id,

        /** Логин/имя пользователя */
        String username,

        /** Идентификатор активного юнита игрока, если выбран; иначе null */
        Long activeUnitId
) {
    /**
     * Преобразует доменную модель Player в DTO PlayerResponse.
     *
     * @param player объект доменной модели Player
     * @return DTO PlayerResponse с данными игрока
     */
    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getActiveUnit() != null ? player.getActiveUnit().getId() : null
        );
    }
}
