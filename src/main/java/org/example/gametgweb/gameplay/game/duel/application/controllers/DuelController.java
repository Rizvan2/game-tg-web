package org.example.gametgweb.gameplay.game.duel.application.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.domain.exception.GameAlreadyExistsException;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelManager;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер, отвечающий за запуск и участие в дуэлях между игроками.
 * <p>
 * Обрабатывает запросы от клиента, связанные с созданием или присоединением к дуэли.
 */
@RestController
@Slf4j
public class DuelController {

    /** Сервис, управляющий логикой дуэлей и игровыми сессиями */
    private final DuelManager duelManager;

    @Autowired
    public DuelController(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    /**
     * Запускает или присоединяет игрока к существующей дуэли по коду игры.
     *
     * <p>Если дуэль с данным кодом существует — игрок присоединяется к ней.
     * Если нет — создаётся новая игра, где текущий игрок становится первым участником.
     *
     * @param gameCode код игры, введённый пользователем (используется для поиска или создания дуэли)
     * @param playerDetails информация о текущем авторизованном пользователе (Spring Security)
     * @return текстовое сообщение о результате операции (например, "Вы создали игру" или "Вы присоединились к дуэли")
     */
    @PostMapping("/StartDuel")
    public String duel(@RequestParam String gameCode,
                       @AuthenticationPrincipal PlayerDetails playerDetails) {
        // Передаём управление в бизнес-логику дуэлей
        return duelManager.joinOrCreateGame(gameCode, getPlayerId(playerDetails));
    }

    @PostMapping("/CreateDuel")
    public String createDuel(@RequestParam String gameCode,
                                        @AuthenticationPrincipal PlayerDetails playerDetails) {
        try {
            log.info(gameCode);
            return duelManager.createGame(gameCode, getPlayerId(playerDetails));
        } catch (GameAlreadyExistsException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/JoinDuel")
    public String joinDuel(@RequestParam String gameCode,
                           @AuthenticationPrincipal PlayerDetails playerDetails) {
        // Передаём управление в бизнес-логику дуэлей
        return duelManager.joinGame(gameCode, getPlayerId(playerDetails));
    }

    private Long getPlayerId(PlayerDetails playerDetails) {
        return playerDetails.playerEntity().getId();
    }
}
