package org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository;

import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
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
public interface JpaPlayerRepository extends JpaRepository<PlayerEntity, Long> {
    /**
     * Находит игрока по его уникальному имени пользователя (username).
     *
     * @param username уникальное имя игрока
     * @return {@link Optional} с {@link PlayerEntity}, если игрок найден,
     *         или пустой {@link Optional}, если игрока с таким username нет
     */
    Optional<PlayerEntity> findByUsername(String username);
}
