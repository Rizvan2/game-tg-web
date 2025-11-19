package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.UnitMapper;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;

public class PlayerMapper {
    /**
     * Преобразует {@link PlayerEntity} в доменную модель {@link Player}.
     * <p>
     * GameSession не заполняется, чтобы избежать циклических зависимостей.
     *
     * @param pe JPA-сущность игрока
     * @return доменная модель {@link Player}
     */
    public static Player toDomain(PlayerEntity pe) {
        Unit activeUnit = pe.getActiveUnitEntity() != null
                ? UnitMapper.toDomain(pe.getActiveUnitEntity())
                : null;

        return new Player(
                pe.getId(),
                pe.getUsername(),
                null, // GameSession заполняется выше, чтобы не делать рекурсию
                activeUnit
        );
    }

    /**
     * Преобразует доменную модель {@link Player} в JPA-сущность {@link PlayerEntity}.
     * <p>
     * Устанавливает обратную связь с {@link GameSessionEntity}.
     *
     * @param p доменная модель игрока
     * @param gameSessionEntity ссылка на игровую сессию (для связи OneToMany)
     * @return JPA-сущность игрока
     */
    public static PlayerEntity mapPlayerToEntity(Player p
            , GameSessionEntity gameSessionEntity, JpaPlayerRepository playerRepo) {
        PlayerEntity pe;

        if (p.getId() != null) {
            // Достаем существующую сущность из БД, чтобы пароль остался
            pe = playerRepo.findById(p.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Player not found"));
            pe.setUsername(p.getUsername());
        } else {
            pe = new PlayerEntity();
            pe.setUsername(p.getUsername());
            pe.setPassword("default"); // или бросить исключение, если это невозможно
        }

        pe.setGameSessionEntity(gameSessionEntity);
        if (p.getActiveUnit() != null) {
            pe.setActiveUnitEntity(UnitMapper.toEntity(p.getActiveUnit()));
        }
        return pe;
    }
}
