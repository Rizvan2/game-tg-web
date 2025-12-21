package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация доменного репозитория {@link PlayerRepository} для работы с игроками.
 * <p>
 * Отвечает за маппинг между доменной моделью {@link Player} и JPA-сущностью {@link PlayerEntity}.
 * Управляет сохранением, обновлением, удалением и поиском игроков, не затрагивая чувствительные данные (пароль).
 */
@Service
public class PlayerRepositoryImpl implements PlayerRepository {

    private final JpaPlayerRepository jpaPlayerRepository;

    @Autowired
    public PlayerRepositoryImpl(JpaPlayerRepository jpaPlayerRepository) {
        this.jpaPlayerRepository = jpaPlayerRepository;
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
