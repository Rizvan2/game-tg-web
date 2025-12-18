package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.gameplay.game.duel.api.dto.PlayerUpdateDto;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;

/**
 * Маппер для преобразования между доменной моделью {@link Player} и JPA-сущностью {@link PlayerEntity}.
 * <p>
 * Используется для изоляции доменной логики от инфраструктуры.
 * Обрабатывает также активного юнита игрока ({@link PlayerUnitMapper}), включая текущее состояние и связь с шаблоном юнита.
 */
public class PlayerMapper {
    /**
     * Преобразует JPA-сущность {@link PlayerEntity} в доменную модель {@link Player}.
     *
     * @param pe JPA-сущность игрока, полученная из базы данных
     * @return доменная модель {@link Player}, отражающая бизнес-логику
     */
    public static Player toDomain(PlayerEntity pe) {
        if (pe == null) return null;

        return new Player(
                pe.getId(),
                pe.getUsername(),
                pe.getGameSessionEntity() != null ? GameSessionMapper.toDomain(pe.getGameSessionEntity()) : null,
                pe.getActiveUnitEntity() != null ? PlayerUnitMapper.toDomain(pe.getActiveUnitEntity()) : null
        );
    }

    /**
     * Преобразует доменную модель {@link Player} в DTO {@link PlayerUpdateDto}.
     * <p>
     * DTO содержит только поля, которые могут быть изменены в БД:
     * id, username, gameSessionId, activeUnitId.
     * Поля gameSessionId и activeUnitId могут быть null, если соответствующие объекты отсутствуют.
     * <p>
     * Используется при обновлении существующего {@link PlayerEntity} через репозиторий.
     *
     * @param player доменная модель с обновляемыми полями
     * @return {@link PlayerUpdateDto} с данными для обновления сущности
     */
    public static PlayerUpdateDto toDto(Player player) {
        Long gameSessionId = player.getGameSession() != null ? player.getGameSession().getId() : null;
        Long activeUnitId = player.getActiveUnit().map(PlayerUnit::getId).orElse(null);

        return new PlayerUpdateDto(
                player.getId(),
                player.getUsername(),
                gameSessionId,
                activeUnitId);
    }
}
