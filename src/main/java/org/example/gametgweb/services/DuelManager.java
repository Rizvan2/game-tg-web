package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.gameSession.GameSessionEntity;
import org.example.gametgweb.gameplay.game.entity.player.PlayerEntity;
import org.example.gametgweb.repository.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервисный менеджер для управления созданием и присоединением к дуэльным играм.
 * <p>
 * Объединяет логику работы с GameSessionEntity и Player, предоставляя единый метод для контроллеров.
 * Позволяет:
 *  - создать новую игру с первым игроком,
 *  - присоединить игрока к существующей игре,
 *  - возвращать ссылку на страницу игры.
 */
@Service
public class DuelManager {

    private final GameServiceImpl gameService;
    private final GameSessionRepository gameSessionRepository;
    private final PlayerServiceImpl playerService;

    @Autowired
    public DuelManager(GameServiceImpl gameService,
                       GameSessionRepository gameSessionRepository,
                       PlayerServiceImpl playerService) {
        this.gameService = gameService;
        this.gameSessionRepository = gameSessionRepository;
        this.playerService = playerService;
    }

    /**
     * Присоединяет игрока к существующей игре или создаёт новую игру.
     * <p>
     * Метод выступает как основной «оркестратор»:
     * 1. Проверяет, существует ли игра с указанным gameCode, иначе создаёт новую.
     * 2. Привязывает игрока к выбранной игре.
     * 3. Формирует ссылку на страницу игры.
     *
     * @param gameCode Код игры, по которому игрок подключается или создаётся игра
     * @param playerId ID игрока, который хочет присоединиться
     * @return Ссылка на страницу игры, например "/gameplay.html?id=123"
     * @throws IllegalArgumentException если игрок с указанным ID не найден
     */
    public String joinOrCreateGame(String gameCode, Long playerId) {
        GameSessionEntity game = findOrCreateGame(gameCode, playerId);
        attachPlayerToGame(playerId, game);
        return buildGameLink(game);
    }

    /** Проверяет, есть ли игра с таким кодом, иначе создаёт новую */
    private GameSessionEntity findOrCreateGame(String gameCode, Long playerId) {
        return gameSessionRepository.findGameByGameCode(gameCode)
                .orElseGet(() -> gameService.createGame(gameCode, playerId));
    }

    /** Привязывает игрока к игре */
    private void attachPlayerToGame(Long playerId, GameSessionEntity game) {
        PlayerEntity playerEntity = playerService.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
        playerEntity.setGameSessionEntity(game);
        game.setPlayer(playerEntity);
    }

    /** Формирует ссылку на страницу игры */
    private String buildGameLink(GameSessionEntity game) {
        return "/gameplay.html?id=" + game.getId();
    }

    public GameSessionEntity findGameByGameCode(String gameCode) {
        return gameService.findGameByGameCode(gameCode);
    }

}

