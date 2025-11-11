package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.gameSession.GameSessionEntity;
import org.example.gametgweb.gameplay.game.entity.gameSession.GameState;
import org.example.gametgweb.gameplay.game.entity.player.PlayerEntity;
import org.example.gametgweb.repository.GameSessionRepository;
import org.example.gametgweb.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для работы с игровыми сессиями и игроками.
 * <p>
 * Реализует интерфейс {@link GameService}. Основная логика включает:
 * - создание, поиск и удаление игр,
 * - привязку игроков к сессиям,
 * - сохранение и обновление состояния игры.
 * <p>
 * Методы работают с {@link GameSessionRepository} и {@link PlayerRepository}.
 */
@Service
public class GameServiceImpl implements GameService {

    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public GameServiceImpl(GameSessionRepository gameSessionRepository, PlayerRepository playerRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Находит игру по её уникальному коду.
     *
     * @param gameCode уникальный код игры
     * @return {@link GameSessionEntity} с указанным кодом
     * @throws IllegalArgumentException если игра не найдена
     */
    @Override
    public GameSessionEntity findGameByGameCode(String gameCode) {
        return gameSessionRepository.findGameByGameCode(gameCode).orElseThrow(() -> new IllegalArgumentException("Invalid game code: " + gameCode));
    }

    /**
     * Сохраняет или обновляет игровую сессию в базе данных.
     *
     * @param game игровая сессия для сохранения
     */
    @Override
    public void setGame(GameSessionEntity game) {
        gameSessionRepository.save(game);
    }

    /**
     * Удаляет игру по её ID.
     *
     * @param id уникальный идентификатор игры
     */
    @Override
    public void deleteGame(Long id) {
        gameSessionRepository.deleteById(id);
    }

    /**
     * Создаёт новую игру или возвращает существующую, а затем привязывает к ней игрока.
     * <p>
     * Метод выполняется в рамках одной транзакции:
     * - если создание игры или привязка игрока завершится ошибкой,
     *   изменения в базе данных будут автоматически откатаны.
     * <p>
     * Логика:
     * 1. Проверяет, есть ли игра с указанным кодом `gameCode`.
     *    - Если игра существует, возвращает её.
     *    - Если нет, создаёт новую игру с состоянием {@link GameState#WAITING}.
     * 2. Привязывает игрока с `playerId` к найденной или созданной игре.
     *
     * @param gameCode уникальный код игры для подключения игроков
     * @param playerId ID игрока, который создаёт игру или присоединяется
     * @return {@link GameSessionEntity} — существующая или новая игровая сессия с привязанным игроком
     * @throws IllegalArgumentException если игрок с указанным ID не найден
     */
    @Override
    @Transactional
    public GameSessionEntity createGame(String gameCode, Long playerId) {
        GameSessionEntity game = findOrCreateGameSession(gameCode, playerId);
        attachPlayerToGame(playerId, game);
        return game;
    }

    /** Проверяет, есть ли игра с таким кодом, иначе создаёт новую */
    private GameSessionEntity findOrCreateGameSession(String gameCode, Long playerId) {
        return gameSessionRepository.findGameByGameCode(gameCode)
                .orElseGet(() -> createNewGameSession(gameCode, playerId));
    }

    /** Создаёт новую игру и сохраняет её */
    private GameSessionEntity createNewGameSession(String gameCode, Long playerId) {
        GameSessionEntity game = new GameSessionEntity(gameCode, GameState.WAITING);
        gameSessionRepository.save(game); // сохраняем до привязки игрока, чтобы получить ID при необходимости
        return game;
    }

    /** Привязывает игрока к игре и наоборот */
    private void attachPlayerToGame(Long playerId, GameSessionEntity game) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        player.setGameSessionEntity(game);
        game.setPlayer(player);
    }

    /**
     * Получает существующую игру по коду или создаёт новую.
     *
     * @param gameCode уникальный код игры
     * @return {@link GameSessionEntity} — существующая или новая сессия
     */
    @Override
    public GameSessionEntity getOrCreateGame(String gameCode) {
        // Проверяем, есть ли уже такая игра
        Optional<GameSessionEntity> existing = gameSessionRepository.findGameByGameCode(gameCode);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Если нет — создаём новую
        GameSessionEntity newGame = new GameSessionEntity(gameCode, GameState.WAITING);
        return gameSessionRepository.save(newGame);
    }

    /**
     * Обновляет состояние игры в базе данных.
     *
     * @param game игровая сессия для обновления
     */
    @Override
    public void updateGame(GameSessionEntity game) {
        this.gameSessionRepository.save(game);
    }
}
