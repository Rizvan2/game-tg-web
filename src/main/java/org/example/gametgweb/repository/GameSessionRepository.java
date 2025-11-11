package org.example.gametgweb.repository;

import org.example.gametgweb.gameplay.game.entity.gameSession.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link GameSessionEntity}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * дополнительные методы поиска игровой сессии по ID или по уникальному коду игры.
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSessionEntity, Long> {

    /**
     * Находит игровую сессию по уникальному коду игры (gameCode).
     *
     * @param gameCode уникальный код игры
     * @return {@link Optional} с {@link GameSessionEntity}, если найдена,
     *         или пустой {@link Optional}, если сессия с таким кодом отсутствует
     */
    Optional<GameSessionEntity> findGameByGameCode(String gameCode);
}
