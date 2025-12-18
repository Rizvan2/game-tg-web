package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.gameplay.game.duel.api.dto.GameSessionEntityDto;
import org.example.gametgweb.gameplay.game.duel.api.dto.PlayerUpdateDto;
import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между доменной моделью {@link GameSession}
 * и DTO/сущностью для слоя инфраструктуры {@link GameSessionEntityDto} / {@link GameSessionEntity}.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>Конвертацию JPA-сущностей в доменные модели.</li>
 *     <li>Конвертацию доменных моделей в DTO для передачи данных между слоями.</li>
 *     <li>Изоляцию бизнес-логики от деталей хранения данных (ORM, база).</li>
 * </ul>
 * <p>
 * Примечание: этот класс не изменяет состояние сущностей, а только преобразует данные.
 */
public class GameSessionMapper {

    /**
     * Преобразует {@link GameSessionEntity} в доменную модель {@link GameSession}.
     * <p>
     * Если входная сущность равна null, возвращается null.
     * Если список игроков в сущности null, в доменной модели используется пустой список.
     *
     * @param entity объект JPA-сущности, полученный из базы данных
     * @return доменная модель {@link GameSession}, отражающая бизнес-логику
     */
    public static GameSession toDomain(GameSessionEntity entity) {
        if (entity == null) return null;

        List<Player> players = entity.getPlayers() != null
                ? entity.getPlayers().stream()
                .map(PlayerMapper::toDomain)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new GameSession(
                entity.getId(),
                entity.getGameCode(),
                entity.getState(),
                players
        );
    }

    /**
     * Преобразует доменную модель {@link GameSession} в DTO {@link GameSessionEntityDto}.
     * <p>
     * Используется для передачи данных о сессии и игроках на слой инфраструктуры или к API.
     * В случае если список игроков пустой или отсутствует, возвращается пустой список.
     *
     * @param domain доменная модель {@link GameSession}, содержащая данные сессии и игроков
     * @return DTO {@link GameSessionEntityDto}, содержащий идентификатор сессии, код игры,
     *         состояние и список игроков в виде {@link PlayerUpdateDto}; если domain равен null,
     *         возвращается null
     */
    public static GameSessionEntityDto toDto (GameSession domain) {
        if (domain == null) return null;
        GameSessionEntityDto entity = new GameSessionEntityDto(
                domain.getId(),
                domain.getGameCode(),
                domain.getState(),
                new ArrayList<>()
        );

        if (domain.getPlayers() != null) {

            // Важно .collect(Collectors.toList()); возвращает не мутабельный лист чтобы его можно было изменять, так и задумано
            List<PlayerUpdateDto> playerEntities = domain.getPlayers().stream()
                    .map(PlayerMapper::toDto)
                    .collect(Collectors.toList());

            return new GameSessionEntityDto(
                    domain.getId(),
                    domain.getGameCode(),
                    domain.getState(),
                    playerEntities
            );
        }

        return entity;
    }
}
