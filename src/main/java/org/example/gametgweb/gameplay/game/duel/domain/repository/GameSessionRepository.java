package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository {

    /**
     * Находит игру по коду.
     *
     * @param gameCode код игры
     * @return объект {@link GameSession}, если найден
     */
    Optional<GameSession> findByGameCode(String gameCode);

    List<GameSession> findAll();
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
    GameSession save(GameSession game);


    /**
     * Удаляет игру по её идентификатору.
     *
     * @param id идентификатор игры
     */
    void deleteGame(Long id);

    default void updateOrSaveGame(GameSession game) {
        if (game.getId() != null) {
            updateGame(game);
        } else {
            save(game);
        }
    }
}
