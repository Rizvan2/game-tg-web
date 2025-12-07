package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;

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
                PlayerUnitMapper.toDomain(pe.getActiveUnitEntity())
        );
    }

    /**
     * Преобразует доменную модель {@link Player} в JPA-сущность {@link PlayerEntity}.
     * <p>
     * Использует {@link JpaPlayerRepository} для поиска существующей сущности в базе.
     * Это необходимо, чтобы Hibernate не создавал новую сущность и корректно обновлял существующую.
     *
     * @param player           доменная модель игрока
     * @param playerRepository репозиторий для поиска сущности игрока в базе
     * @return существующая или обновлённая JPA-сущность {@link PlayerEntity}
     * @throws IllegalStateException если игрок с указанным ID не найден в базе
     */
    public static PlayerEntity toEntity(Player player, JpaPlayerRepository playerRepository) {
        PlayerEntity entity = playerRepository.findById(player.getId())
                .orElseThrow(() -> new IllegalStateException("Player not found"));

        entity.setId(player.getId());
        entity.setUsername(player.getUsername());
        entity.setGameSessionEntity(GameSessionMapper.toEntity(player.getGameSession(), playerRepository));
        entity.setActiveUnitEntity(PlayerUnitMapper.toEntity(player.getActiveUnit()));

        return entity;
    }
}
