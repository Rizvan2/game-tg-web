package org.example.gametgweb.repository;

import org.example.gametgweb.gameplay.game.entity.player.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link PlayerEntity}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * дополнительные методы поиска игрока по уникальному имени.
 */
@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    /**
     * Находит игрока по его уникальному имени пользователя (username).
     *
     * @param username уникальное имя игрока
     * @return {@link Optional} с {@link PlayerEntity}, если игрок найден,
     *         или пустой {@link Optional}, если игрока с таким username нет
     */
    Optional<PlayerEntity> findByUsername(String username);
}
