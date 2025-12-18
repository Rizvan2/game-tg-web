package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.example.gametgweb.gameplay.game.duel.api.dto.PlayerUpdateDto;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация доменного репозитория {@link PlayerRepository} для работы с игроками.
 * <p>
 * Отвечает за маппинг между доменной моделью {@link Player} и JPA-сущностью {@link PlayerEntity}.
 * Управляет сохранением, обновлением, удалением и поиском игроков, не затрагивая чувствительные данные (пароль).
 */
@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    private final JpaPlayerRepository jpaPlayerRepository;
    private final JpaGameSessionRepository gameSessionRepository;
    private final JpaPlayerUnitRepository playerUnitRepository;

    @Autowired
    public PlayerRepositoryImpl(JpaPlayerRepository jpaPlayerRepository, JpaGameSessionRepository gameSessionRepository, JpaPlayerUnitRepository playerUnitRepository) {
        this.jpaPlayerRepository = jpaPlayerRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.playerUnitRepository = playerUnitRepository;
    }

    /**
     * Находит игрока по его ID.
     *
     * @param id идентификатор игрока
     * @return {@link Optional} с доменной моделью {@link Player} или пустой, если игрок не найден
     */
    @Override
    public Optional<Player> findById(Long id) {
        return jpaPlayerRepository.findById(id)
                .map(PlayerMapper::toDomain);
    }

    /**
     * Сохраняет изменения игрока в базе данных.
     * <p>
     * Обновляются только username и активный юнит, пароль остаётся неизменным.
     *
     * @param player доменная модель игрока
     * @return обновлённая доменная модель {@link Player}
     * @throws IllegalArgumentException если игрок не найден
     */
//    @Override
//    public Player save(Player player) {
//        PlayerEntity playerEntity = PlayerMapper.toEntityUpdate(player);
//        return PlayerMapper.toDomain(jpaPlayerRepository
//                .save(playerEntity));
//    }

    /**
     * Обновляет игрока в базе данных.
     *
     * @param player доменная модель игрока
     * @return обновлённая доменная модель {@link Player}
     */
    @Override
    public Player update(Player player) {
        PlayerEntity entity = jpaPlayerRepository.findById(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        PlayerUpdateDto playerUpdateDto = PlayerMapper.toDto(player);

        entity.setUsername(playerUpdateDto.username());

        updateGameSessionIfChanged(entity, player, playerUpdateDto);

        PlayerUnit unit = player.getActiveUnit()
                .orElseThrow(() -> new IllegalStateException("Player has no active unit"));

        PlayerUnitEntity playerUnitEntity =
                checkUpdateEntity(unit.getId(), unit);

        entity.setActiveUnitEntity(playerUnitEntity);

        return PlayerMapper.toDomain(jpaPlayerRepository.save(entity));
    }

    private void updateGameSessionIfChanged(PlayerEntity entity, Player player, PlayerUpdateDto dto) {
        // Если у игрока нет текущей сессии, и у передаваемого состояния тоже, то просто не добавляем ее)
        if (entity.getGameSessionEntity() == null && dto.gameSessionId() == null) return;

            GameSessionEntity updatedSession = null;

            if (dto.gameSessionId() != null) {
                // Берем актуальную сессию по ID из DTO
                updatedSession = gameSessionRepository.findById(dto.gameSessionId())
                        .orElseThrow(() -> new IllegalArgumentException("Сессия не найдена"));
                updatedSession.setState(player.getGameSession().getState());
            }

            // Применяем (может быть null)
            entity.setGameSessionEntity(updatedSession);
    }



    private PlayerUnitEntity checkUpdateEntity(Long id, PlayerUnit activeUnit) {
        PlayerUnitEntity entity = playerUnitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Юнит не найден"));

        boolean changed = false;

        if (!Objects.equals(entity.getName(), activeUnit.getName())) {
            entity.setName(activeUnit.getName());
            changed = true;
        }

        if (entity.getMaxHealth() != activeUnit.getMaxHealth()) {
            entity.setMaxHealth(activeUnit.getMaxHealth());
            changed = true;
        }

        if (entity.getHealth() != activeUnit.getHealth()) {
            entity.setHealth(activeUnit.getHealth());
            changed = true;
        }

        if (entity.getDamage() != activeUnit.getDamage()) {
            entity.setDamage(activeUnit.getDamage());
            changed = true;
        }

        if (!Objects.equals(entity.getImagePath(), activeUnit.getImagePath())) {
            entity.setImagePath(activeUnit.getImagePath());
            changed = true;
        }

        if (changed) {
            entity = playerUnitRepository.save(entity);
        }

        return entity;
    }


    /**
     * Удаляет игрока из базы данных.
     *
     * @param id уникальный идентификатор игрока
     */
    @Override
    public void delete(long id) {
        PlayerEntity entity = jpaPlayerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        jpaPlayerRepository.delete(entity);
    }

    /**
     * Находит игрока по имени пользователя.
     *
     * @param username имя пользователя
     * @return доменная модель {@link Player}
     * @throws IllegalArgumentException если игрок с таким именем не найден
     */
    @Override
    public Player findByUsername(String username) {
        PlayerEntity entity = jpaPlayerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден: " + username));
        return PlayerMapper.toDomain(entity);
    }

    /**
     * Получает список всех игроков.
     *
     * @return список доменных моделей {@link Player}
     */
    public List<Player> findAll() {
        return jpaPlayerRepository.findAll().stream()
                .map(PlayerMapper::toDomain)
                .collect(Collectors.toList());
    }
}
