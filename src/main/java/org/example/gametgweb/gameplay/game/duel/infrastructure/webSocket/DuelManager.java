package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.domain.repository.GameSessionRepositoryImpl;
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

    private final GameSessionRepositoryImpl gameService;

    @Autowired
    public DuelManager(GameSessionRepositoryImpl gameService) {
        this.gameService = gameService;
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
     * @return Ссылка на страницу игры, например "/duel-battle.html?id=123"
     * @throws IllegalArgumentException если игрок с указанным ID не найден
     */
    public String joinOrCreateGame(String gameCode, Long playerId) {
        return buildGameLink(gameService.joinOrCreateGame(gameCode,playerId));
    }
    /** Формирует ссылку на страницу игры */
    private String buildGameLink(GameSession game) {
        return "/duel-battle.html?id=" + game.getId();
    }
}

