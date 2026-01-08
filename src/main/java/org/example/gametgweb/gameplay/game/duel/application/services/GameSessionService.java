package org.example.gametgweb.gameplay.game.duel.application.services;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.api.dto.GameSessionDto;
import org.example.gametgweb.gameplay.game.duel.domain.exception.GameAlreadyExistsException;
import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.GameSessionRepository;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepository;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления игровыми сессиями дуэли.
 * <p>
 * Основные функции:
 * - создание новой игры;
 * - присоединение игроков к существующим сессиям;
 * - обновление состояния игры;
 * - управление связью игрок-сессия.
 * <p>
 * Работает с доменной моделью {@link GameSession} и репозиториями {@link GameSessionRepository} и {@link PlayerRepository}.
 */
@Service
@Slf4j
public class GameSessionService {

    private final GameSessionRepository repository;
    private final PlayerRepository playerRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param repository       репозиторий для работы с игровыми сессиями
     * @param playerRepository репозиторий для работы с игроками
     */
    public GameSessionService(GameSessionRepository repository, PlayerRepository playerRepository) {
        this.repository = repository;
        this.playerRepository = playerRepository;
    }


    /**
     * Позволяет игроку присоединиться к существующей сессии или создать новую,
     * если сессия с указанным кодом не найдена.
     *
     * @param gameCode уникальный код сессии
     * @param playerId идентификатор игрока
     * @return обновлённая доменная модель {@link GameSession} после добавления игрока
     * @throws IllegalArgumentException если playerId = null или игрок не найден
     */
    @Transactional
    public GameSession joinOrCreateGame(String gameCode, Long playerId) {
        validatePlayerId(playerId);

        GameSession game = findOrCreateGameSession(gameCode);

        return attachAndSaveOrUpdate(playerId, game);
    }

    /**
     * Создаёт новую игровую сессию с указанным кодом и сразу прикрепляет к ней игрока.
     * <p>
     * Если сессия с таким кодом уже существует, выбрасывает {@link GameAlreadyExistsException}.
     *
     * @param gameCode код новой сессии
     * @param playerId ID игрока
     * @return сохранённая доменная модель {@link GameSession} с присвоенным ID
     */
    @Transactional
    public GameSession createGameAndAttachPlayer(String gameCode, Long playerId) {
        validatePlayerId(playerId);

        if (repository.findByGameCode(gameCode).isPresent()) {
            throw new GameAlreadyExistsException(gameCode);
        }

        GameSession game = repository.save(createNewGameSession(gameCode));

        log.info("Создана игра: id={}, gameCode={}, players={}",
                game.getId(), game.getGameCode(), game.getPlayers().size());

        attachAndSaveOrUpdate(playerId, game);

        return game;
    }

    /**
     * Возвращает список активных игровых сессий.
     *
     * <p>
     * Загружает все сессии из репозитория,
     * логирует игроков в каждой сессии
     * и маппит результат в {@link GameSessionDto}.
     * </p>
     *
     * @return список DTO игровых сессий
     */
    @Transactional(readOnly = true)
    public List<GameSessionDto> getAllSessions() {
        return repository.findAll().stream()
                .peek(gs -> {
                    String playerNames = gs.getPlayers().stream()
                            .map(Player::getUsername)
                            .collect(Collectors.joining(", "));
                    log.info("Game {} has {} players: {}", gs.getGameCode(), gs.getPlayers().size(), playerNames);
                })
                .map(gs -> new GameSessionDto(gs.getGameCode(), gs.getPlayers().size()))
                .toList();
    }

    /**
     * Вспомогательный метод: прикрепляет игрока к сессии и сохраняет изменения.
     *
     * @param playerId ID игрока
     * @param game     доменная модель игры
     * @return обновлённая модель {@link GameSession}
     */
    private GameSession attachAndSaveOrUpdate(Long playerId, GameSession game) {
        attachPlayerToGame(playerId, game);
        log.info("Перед сохранением: {} игроков в игре {}", game.getPlayers().size(), game.getGameCode());

        repository.updateOrSaveGame(game);
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
     * Позволяет игроку присоединиться к уже существующей сессии.
     *
     * @param gameCode код сессии
     * @param playerId ID игрока
     * @return обновлённая модель {@link GameSession}
     * @throws IllegalArgumentException если игрок не найден или сессия не существует
     */
    @Transactional
    public GameSession joinGame(String gameCode, Long playerId) {
        validatePlayerId(playerId);
        GameSession game = repository.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Комната не найдена или не существует"));

        return attachAndSaveOrUpdate(playerId, game);
    }

    /**
     * Находит существующую сессию по коду или создаёт новую.
     *
     * @param gameCode код сессии
     * @return доменная модель {@link GameSession}
     */
    private GameSession findOrCreateGameSession(String gameCode) {
        return repository.findByGameCode(gameCode)
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
        return new GameSession(gameCode, GameState.WAITING);
    }

    /**
     * Проверяет, что передан корректный идентификатор игрока.
     *
     * @param playerId ID игрока
     * @throws IllegalArgumentException если playerId = null
     */
    private void validatePlayerId(Long playerId) {
        if (playerId == null) throw new IllegalArgumentException("Player ID must not be null");
    }
}
