package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.UnitMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    private final JpaPlayerRepository jpaRepository;

    public PlayerRepositoryImpl(JpaPlayerRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Находит игрока по его ID.
     *
     * @param id идентификатор игрока
     * @return {@link Optional} с доменной моделью {@link Player} или пустой, если игрок не найден
     */
    @Override
    public Optional<Player> findById(Long id) {
        return jpaRepository.findById(id)
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
    @Override
    public Player savePlayer(Player player) {
        PlayerEntity existing = jpaRepository.findById(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        existing.setUsername(player.getUsername());
        if (player.getActiveUnit() != null) {
            existing.setActiveUnitEntity(UnitMapper.toEntity(player.getActiveUnit()));
        }

        PlayerEntity saved = jpaRepository.save(existing);
        return PlayerMapper.toDomain(saved);
    }

    /**
     * Удаляет игрока из базы данных.
     *
     * @param player доменная модель игрока
     */
    @Override
    public void deletePlayer(Player player) {
        PlayerEntity entity = PlayerMapper.mapPlayerToEntity(player, null, jpaRepository);
        jpaRepository.delete(entity);
    }

    /**
     * Обновляет игрока в базе данных.
     *
     * @param player доменная модель игрока
     * @return обновлённая доменная модель {@link Player}
     */
    @Override
    public Player updatePlayer(Player player) {
        PlayerEntity entity = PlayerMapper.mapPlayerToEntity(player, null, jpaRepository);
        PlayerEntity updated = jpaRepository.save(entity);
        return PlayerMapper.toDomain(updated);
    }

    /**
     * Устанавливает состояние игрока.
     * <p>
     * Использует {@link #updatePlayer(Player)} как основную операцию.
     *
     * @param player доменная модель игрока
     * @return обновлённая доменная модель {@link Player}
     */
    @Override
    public Player setPlayer(Player player) {
        return updatePlayer(player);
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
        PlayerEntity entity = jpaRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден: " + username));
        return PlayerMapper.toDomain(entity);
    }

    /**
     * Получает список всех игроков.
     *
     * @return список доменных моделей {@link Player}
     */
    public List<Player> findAll() {
        return jpaRepository.findAll().stream()
                .map(PlayerMapper::toDomain)
                .collect(Collectors.toList());
    }
}
