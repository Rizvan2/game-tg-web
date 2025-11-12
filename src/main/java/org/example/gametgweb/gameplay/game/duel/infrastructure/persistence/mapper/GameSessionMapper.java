package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между доменной моделью {@link GameSession}
 * и JPA-сущностью {@link GameSessionEntity}.
 * <p>
 * Используется в инфраструктурном слое для изоляции доменной логики
 * от деталей хранения данных (ORM, SQL и т.д.).
 */
public class GameSessionMapper {

    /**
     * Преобразует {@link GameSessionEntity} в доменную модель {@link GameSession}.
     *
     * @param entity объект JPA-сущности, полученный из базы данных
     * @return доменная модель {@link GameSession}, отражающая бизнес-логику
     */
    public static GameSession toDomain(GameSessionEntity entity) {
        if (entity == null) return null;

        List<Player> players = entity.getPlayers() != null
                ? entity.getPlayers().stream()
                .map(PlayerMapper::mapPlayerToDomain)
                .collect(Collectors.toList())
                : List.of();

        return new GameSession(
                entity.getId(),
                entity.getGameCode(),
                entity.getState(),
                players
        );
    }

    /**
     * Преобразует доменную модель {@link GameSession} в JPA-сущность {@link GameSessionEntity}.
     *
     * @param domain доменная модель, содержащая бизнес-данные
     * @return JPA-сущность {@link GameSessionEntity}, пригодная для сохранения в базу данных
     */
    public static GameSessionEntity toEntity(GameSession domain) {
        if (domain == null) return null;

        GameSessionEntity entity = new GameSessionEntity();
        entity.setId(domain.getId());
        entity.setGameCode(domain.getGameCode());
        entity.setState(domain.getState());

        if (domain.getPlayers() != null) {
            List<PlayerEntity> playerEntities = domain.getPlayers().stream()
                    .map(p -> PlayerMapper.mapPlayerToEntity(p, entity))
                    .collect(Collectors.toList());
            entity.setPlayers(playerEntities);
        }

        return entity;
    }
}
