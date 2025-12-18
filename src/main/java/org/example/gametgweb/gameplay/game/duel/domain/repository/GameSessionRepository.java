package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;

import java.util.Optional;

public interface GameSessionRepository {

    /**
     * Находит игру по коду.
     *
     * @param gameCode код игры
     * @return объект {@link GameSession}, если найден
     */
    Optional<GameSession> findByGameCode(String gameCode);

    /**
     * Обновляет состояние существующей игры.
     *
     * @param game игра для обновления
     */
    void updateGame(GameSession game);

    /**
     * Сохраняет или устанавливает текущее состояние игры.
     *
     * @param game игра для сохранения
     */
    void save(GameSession game);

    /**
     * Удаляет игру по её идентификатору.
     *
     * @param id идентификатор игры
     */

    /**
     * Создаёт новую игру с заданным кодом и первым игроком.
     *
     * @param gameCode код игры
     * @param playerId идентификатор первого игрока
     * @return созданная {@link GameSession}
     */
    GameSession joinOrCreateGame(String gameCode, Long playerId);

    void deleteGame(Long id);
}
