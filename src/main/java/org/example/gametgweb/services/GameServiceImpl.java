package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.GameState;
import org.example.gametgweb.gameplay.game.entity.GameSession;
import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.repository.GameSessionRepository;
import org.example.gametgweb.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GameServiceImpl implements GameService {

    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public GameServiceImpl(GameSessionRepository gameSessionRepository, PlayerRepository playerRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public GameSession getGame(Long id) {
        return gameSessionRepository.getGameById(id);
    }

    @Override
    public void setGame(GameSession game) {
        gameSessionRepository.save(game);
    }

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
     *    - Если нет, создаёт новую игру с состоянием {@link org.example.gametgweb.gameplay.game.GameState#WAITING}.
     * 2. Привязывает игрока с `playerId` к найденной или созданной игре.
     *
     * @param gameCode уникальный код игры для подключения игроков
     * @param playerId ID игрока, который создаёт игру или присоединяется
     * @return {@link GameSession} — существующая или новая игровая сессия с привязанным игроком
     * @throws IllegalArgumentException если игрок с указанным ID не найден
     */
    @Override
    @Transactional
    public GameSession createGame(String gameCode, Long playerId) {
        GameSession game = findOrCreateGameSession(gameCode, playerId);
        attachPlayerToGame(playerId, game);
        return game;
    }

    /** Проверяет, есть ли игра с таким кодом, иначе создаёт новую */
    private GameSession findOrCreateGameSession(String gameCode, Long playerId) {
        return gameSessionRepository.findGameByGameCode(gameCode)
                .orElseGet(() -> createNewGameSession(gameCode, playerId));
    }

    /** Создаёт новую игру и сохраняет её */
    private GameSession createNewGameSession(String gameCode, Long playerId) {
        GameSession game = new GameSession(gameCode, GameState.WAITING);
        gameSessionRepository.save(game); // сохраняем до привязки игрока, чтобы получить ID при необходимости
        return game;
    }

    /** Привязывает игрока к игре */
    private void attachPlayerToGame(Long playerId, GameSession game) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found with id: " + playerId));
        player.setGame(game);
    }


    @Override
    public GameSession getOrCreateGame(String gameCode) {
        // Проверяем, есть ли уже такая игра
        Optional<GameSession> existing = gameSessionRepository.findGameByGameCode(gameCode);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Если нет — создаём новую
        GameSession newGame = new GameSession(gameCode, GameState.WAITING);
        return gameSessionRepository.save(newGame);
    }

    @Override
    public void updateGame(GameSession game) {
        this.gameSessionRepository.save(game);
    }
}
