package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.UnitMapper;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;

/**
 * Маппер для преобразования игроков между доменной моделью {@link Player}
 * и JPA-сущностью {@link PlayerEntity}.
 * <p>
 * Обрабатывает активного юнита игрока (PlayerUnit), включая его текущее состояние
 * и связь с шаблоном юнита.
 */
public class PlayerMapper {
    /**
     * Преобразует JPA-сущность {@link PlayerEntity} в доменную модель {@link Player}.
     * <p>
     * Восстанавливает активного юнита игрока на основе {@link PlayerUnitEntity}:
     * - создаёт PlayerUnit на основе шаблона
     * - переносит состояние (здоровье, урон, имя, изображение)
     *
     * @param pe JPA-сущность игрока
     * @return доменная модель игрока
     */
    public static Player toDomain(PlayerEntity pe) {

        PlayerUnit activeUnit = null;

        if (pe.getActiveUnitEntity() != null) {
            PlayerUnitEntity u = pe.getActiveUnitEntity();

            activeUnit = new PlayerUnit(
                    UnitMapper.toDomain(u.getTemplate())
            );

            // заливаем кастомные свойства
            playerUnitSetStates(activeUnit, u);
        }

        return new Player(
                pe.getId(),
                pe.getUsername(),
                pe.getGameSessionEntity() != null ? GameSessionMapper.toDomain(pe.getGameSessionEntity()) : null,
                activeUnit
        );
    }


    /**
     * Преобразует доменную модель {@link Player} в JPA-сущность {@link PlayerEntity}.
     * <p>
     * Особенности:
     * <ul>
     *     <li>Сохраняет существующий пароль игрока, извлекая сущность из БД по ID.</li>
     *     <li>Обрабатывает активного юнита игрока: ищет по ID или создаёт новый {@link PlayerUnitEntity}.</li>
     *     <li>Сохраняет новый юнит в базе перед присвоением, если он ещё не существует.</li>
     *     <li>Устанавливает связь с {@link GameSessionEntity} для OneToMany.</li>
     * </ul>
     *
     * @param p                    доменная модель игрока
     * @param gameSessionEntity    игровая сессия
     * @param playerRepo           репозиторий игроков
     * @param playerUnitRepository репозиторий PlayerUnit
     * @return JPA-сущность игрока
     */
    public static PlayerEntity mapPlayerToEntity(Player p
            , GameSessionEntity gameSessionEntity, JpaPlayerRepository playerRepo, JpaPlayerUnitRepository playerUnitRepository) {

        // Достаем существующую сущность из БД, чтобы пароль остался
        PlayerEntity playerEntity = playerRepo.findById(p.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        playerEntity.setGameSessionEntity(gameSessionEntity);

        if (p.getActiveUnit() != null) {
            PlayerUnit playerUnit = p.getActiveUnit();

            PlayerUnitEntity activeUnit = playerUnit.getId() != null
                    ? playerUnitRepository.findById(playerUnit.getId())
                    .orElseThrow(() -> new IllegalArgumentException("PlayerUnit not found"))
                    : new PlayerUnitEntity(UnitMapper.toEntity(playerUnit.getTemplate()));

            playerUnitEntitySetStates(activeUnit, playerUnit);
            // Сохраняем новый юнит в базе перед присвоением, если нужен
            if (activeUnit.getId() == null) {
                playerUnitRepository.save(activeUnit);
            }

            playerEntity.setActiveUnitEntity(activeUnit);
        }

        return playerEntity;
    }

    /**
     * Устанавливает состояние доменной модели {@link PlayerUnit} из {@link PlayerUnitEntity}.
     *
     * @param activeUnit доменный юнит
     * @param u          сущность юнита из базы
     */
    private static void playerUnitSetStates(PlayerUnit activeUnit, PlayerUnitEntity u) {
        activeUnit.setHealth(u.getHealth());
        activeUnit.setMaxHealth(u.getMaxHealth());
        activeUnit.setDamage(u.getDamage());
        activeUnit.setName(u.getName());
        activeUnit.setImagePath(u.getImagePath());
    }

    /**
     * Устанавливает состояние сущности {@link PlayerUnitEntity} из доменной модели {@link PlayerUnit}.
     *
     * @param activeUnit сущность юнита
     * @param playerUnit доменный юнит
     */
    private static void playerUnitEntitySetStates(PlayerUnitEntity activeUnit, PlayerUnit playerUnit) {
        activeUnit.setHealth(playerUnit.getHealth());
        activeUnit.setMaxHealth(playerUnit.getMaxHealth());
        activeUnit.setDamage(playerUnit.getDamage());
        activeUnit.setName(playerUnit.getName());
        activeUnit.setImagePath(playerUnit.getImagePath());
    }
}
