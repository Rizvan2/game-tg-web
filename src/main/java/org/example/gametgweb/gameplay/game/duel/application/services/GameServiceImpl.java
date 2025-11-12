package org.example.gametgweb.gameplay.game.duel.application.services;

import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
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
 * Методы работают с {@link JpaGameSessionRepository} и {@link JpaPlayerRepository}.
 */
@Service
public class GameServiceImpl implements GameService {

    private final JpaGameSessionRepository jpaGameSessionRepository;
    private final JpaPlayerRepository jpaPlayerRepository;

    @Autowired
    public GameServiceImpl(JpaGameSessionRepository jpaGameSessionRepository, JpaPlayerRepository jpaPlayerRepository) {
        this.jpaGameSessionRepository = jpaGameSessionRepository;
        this.jpaPlayerRepository = jpaPlayerRepository;
    }

    /**
     * Находит игру по её уникальному коду.
     *
     * @param gameCode уникальный код игры
     * @return {@link GameSessionEntity} с указанным кодом
     * @throws IllegalArgumentException если игра не найдена
     */
    public Optional<GameSessionEntity> findByGameCode(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode);
    }

    /**
     * Сохраняет или обновляет игровую сессию в базе данных.
     *
     * @param game игровая сессия для сохранения
     */
    @Override
    public void save(GameSessionEntity game) {
        jpaGameSessionRepository.save(game);
    }

    /**
     * Удаляет игру по её ID.
     *
     * @param id уникальный идентификатор игры
     */
    @Override
    public void deleteGame(Long id) {
        jpaGameSessionRepository.deleteById(id);
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
        GameSessionEntity game = findOrCreateGameSession(gameCode);
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID must not be null");
        }
        jpaPlayerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player with id " + playerId + " does not exist"));

            // Привязываем игрока к игре
            attachPlayerToGame(playerId, game);
            // Сохраняем изменения в базе через JPA
            save(game);

        return game;
    }

    /** Проверяет, есть ли игра с таким кодом, иначе создаёт новую */
    private GameSessionEntity findOrCreateGameSession(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode)
                .orElseGet(() -> createNewGameSession(gameCode));
    }

    /** Создаёт новую игру и сохраняет её */
    private GameSessionEntity createNewGameSession(String gameCode) {
        GameSessionEntity game = new GameSessionEntity(gameCode, GameState.WAITING);
        jpaGameSessionRepository.save(game); // сохраняем до привязки игрока, чтобы получить ID при необходимости
        return game;
    }

    /** Привязывает игрока к игре и наоборот */
    private void attachPlayerToGame(Long playerId, GameSessionEntity game) {
        PlayerEntity player = jpaPlayerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        game.setPlayer(player);
    }

    /**
     * Обновляет состояние игры в базе данных.
     *
     * @param game игровая сессия для обновления
     */
    @Override
    public void updateGame(GameSessionEntity game) {
        this.jpaGameSessionRepository.save(game);
    }
}
