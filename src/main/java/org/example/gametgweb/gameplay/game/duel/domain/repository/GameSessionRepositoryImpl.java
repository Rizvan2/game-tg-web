package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.GameSessionMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Реализация доменного репозитория {@link GameSessionRepository},
 * отвечающего за управление агрегатом {@link GameSession}.
 * <p>
 * Использует JPA-репозитории и мапперы для изоляции доменного уровня от инфраструктуры.
 * Репозиторий инкапсулирует операции поиска, создания, сохранения и обновления игровых сессий.
 * <p>
 * Главная задача — обеспечение целостности доменной модели при взаимодействии
 * между игроками и игровыми сессиями.
 */
@Repository
public class GameSessionRepositoryImpl implements GameSessionRepository {

    private final JpaGameSessionRepository jpaGameSessionRepository;
    private final PlayerRepositoryImpl playerRepository;
    private final JpaPlayerRepository jpaPlayerRepository;

    public GameSessionRepositoryImpl(JpaGameSessionRepository jpaGameSessionRepository,
                                     PlayerRepositoryImpl playerRepository,
                                     JpaPlayerRepository jpaPlayerRepository) {
        this.jpaGameSessionRepository = jpaGameSessionRepository;
        this.playerRepository = playerRepository;
        this.jpaPlayerRepository = jpaPlayerRepository;
    }

    /**
     * Ищет игровую сессию по коду.
     *
     * @param gameCode уникальный код сессии
     * @return {@link Optional} с найденной игровой сессией или пустой, если не существует
     */
    @Override
    public Optional<GameSession> findByGameCode(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode)
                .map(GameSessionMapper::toDomain);
    }

    /**
     * Позволяет игроку присоединиться к существующей сессии
     * или создать новую, если она не найдена.
     *
     * @param gameCode уникальный код сессии
     * @param playerId идентификатор игрока
     * @return доменная модель {@link GameSession} после обновления
     * @throws IllegalArgumentException если playerId = null или игрок не найден
     */
    @Override
    @Transactional
    public GameSession joinOrCreateGame(String gameCode, Long playerId) {
        GameSession game = findOrCreateGameSession(gameCode);

        if (playerId == null) {
            throw new IllegalArgumentException("Player ID must not be null");
        }

        playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player with id " + playerId + " does not exist"));

        attachPlayerToGame(playerId, game);
        save(game);

        return game;
    }

    /**
     * Находит существующую сессию по коду или создаёт новую.
     *
     * @param gameCode код сессии
     * @return доменная модель {@link GameSession}
     */
    private GameSession findOrCreateGameSession(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode)
                .map(GameSessionMapper::toDomain)
                .orElseGet(() -> createNewGameSession(gameCode));
    }

    /**
     * Создаёт новую игровую сессию в состоянии {@link GameState#WAITING}
     * и сохраняет её в базу.
     *
     * @param gameCode код создаваемой игры
     * @return доменная модель {@link GameSession} с установленным ID
     */
    private GameSession createNewGameSession(String gameCode) {
        GameSession game = new GameSession(gameCode, GameState.WAITING);
        long generatedId = jpaGameSessionRepository
                .save(GameSessionMapper.toEntity(game, jpaPlayerRepository))
                .getId();
        game.setId(generatedId);
        return game;
    }

    /**
     * Привязывает игрока к сессии, если он ещё не добавлен.
     *
     * @param playerId ID игрока
     * @param game     доменная модель игры {@link GameSession}
     * @throws IllegalArgumentException если игрок не найден
     */
    private void attachPlayerToGame(Long playerId, GameSession game) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        if (!game.getPlayers().contains(player)) {
            game.addPlayer(player);
        }
    }

    /**
     * Сохраняет состояние игровой сессии в базе.
     *
     * @param game доменная модель {@link GameSession}
     */
    @Override
    @Transactional
    public void save(GameSession game) {
        var entity = GameSessionMapper.toEntity(game, jpaPlayerRepository);
        jpaGameSessionRepository.save(entity);
    }

    /**
     * Обновляет существующую игровую сессию.
     *
     * @param game доменная модель {@link GameSession}
     */
    @Override
    @Transactional
    public void updateGame(GameSession game) {
        var entity = GameSessionMapper.toEntity(game, jpaPlayerRepository);
        jpaGameSessionRepository.save(entity);
    }

    /**
     * Удаляет игровую сессию по идентификатору.
     *
     * @param id идентификатор игровой сессии
     */
    @Override
    @Transactional
    public void deleteGame(Long id) {
        jpaGameSessionRepository.deleteById(id);
    }
}
