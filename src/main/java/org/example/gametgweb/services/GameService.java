package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.Duel.entity.GameSession;

/**
 * Сервисный интерфейс для управления дуэльными играми ({@link GameSession}).
 * <p>
 * Предоставляет методы для создания, поиска, обновления и удаления игр.
 */
public interface GameService {

    /**
     * Находит игру по коду.
     *
     * @param gameCode код игры
     * @return объект {@link GameSession}, если найден
     */
    GameSession findGameByGameCode(String gameCode);

    /**
     * Сохраняет или устанавливает текущее состояние игры.
     *
     * @param game игра для сохранения
     */
    void setGame(GameSession game);

    /**
     * Удаляет игру по её идентификатору.
     *
     * @param id идентификатор игры
     */
    void deleteGame(Long id);

    /**
     * Создаёт новую игру с заданным кодом и первым игроком.
     *
     * @param gameCode код игры
     * @param playerId идентификатор первого игрока
     * @return созданная {@link GameSession}
     */
    GameSession createGame(String gameCode, Long playerId);

    /**
     * Обновляет состояние существующей игры.
     *
     * @param game игра для обновления
     */
    void updateGame(GameSession game);

    /**
     * Получает существующую игру по коду или создаёт новую, если не найдена.
     *
     * @param gameCode код игры
     * @return {@link GameSession}
     */
    GameSession getOrCreateGame(String gameCode);
}
