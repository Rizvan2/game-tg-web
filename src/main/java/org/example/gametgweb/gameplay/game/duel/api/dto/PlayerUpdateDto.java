package org.example.gametgweb.gameplay.game.duel.api.dto;

/**
 * DTO для обновления игрока в базе данных.
 * <p>
 * Содержит только изменяемые поля сущности {@link org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity}:
 * <ul>
 *     <li>{@code id} — уникальный идентификатор игрока (обязательное поле);</li>
 *     <li>{@code username} — имя пользователя;</li>
 *     <li>{@code gameSessionId} — идентификатор игровой сессии, к которой привязан игрок (может быть {@code null});</li>
 *     <li>{@code activeUnitId} — идентификатор активного юнита игрока (может быть {@code null}).</li>
 * </ul>
 * <p>
 * Используется для передачи данных между слоем доменной логики и инфраструктурой
 * при обновлении состояния игрока без затрагивания других полей сущности.
 */
public record PlayerUpdateDto(Long id, String username, Long gameSessionId, Long activeUnitId) {
}

