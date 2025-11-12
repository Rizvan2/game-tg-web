package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;

public class PlayerMapper {
    /**
     * Преобразует {@link PlayerEntity} в доменную модель {@link Player}.
     * <p>
     * GameSession не заполняется, чтобы избежать циклических зависимостей.
     *
     * @param pe JPA-сущность игрока
     * @return доменная модель {@link Player}
     */
    public static Player mapPlayerToDomain(PlayerEntity pe) {
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
    public static PlayerEntity mapPlayerToEntity(Player p, GameSessionEntity gameSessionEntity) {
        PlayerEntity pe = new PlayerEntity();
        pe.setId(p.getId());
        pe.setUsername(p.getUsername());
        pe.setGameSessionEntity(gameSessionEntity);

        if (p.getActiveUnit() != null)
            pe.setActiveUnitEntity(UnitMapper.toEntity(p.getActiveUnit()));

        return pe;
    }
}
