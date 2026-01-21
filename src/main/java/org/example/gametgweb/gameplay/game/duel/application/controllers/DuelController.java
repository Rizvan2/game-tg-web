package org.example.gametgweb.gameplay.game.duel.application.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.api.dto.GameSessionDto;
import org.example.gametgweb.gameplay.game.duel.domain.exception.GameAlreadyExistsException;
import org.example.gametgweb.gameplay.game.duel.application.services.duel.DuelManager;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер, отвечающий за запуск и участие в дуэлях между игроками.
 * <p>
 * Обрабатывает запросы от клиента, связанные с созданием или присоединением к дуэли.
 */
@RestController
@Slf4j
public class DuelController {

    /**
     * Сервис, управляющий логикой дуэлей и игровыми сессиями
     */
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
     * @param gameCode      код игры, введённый пользователем (используется для поиска или создания дуэли)
     * @param playerDetails информация о текущем авторизованном пользователе (Spring Security)
     * @return текстовое сообщение о результате операции (например, "Вы создали игру" или "Вы присоединились к дуэли")
     */
    @PostMapping("/StartDuel")
    public String duel(@RequestParam String gameCode,
                       @AuthenticationPrincipal PlayerDetails playerDetails) {
        // Передаём управление в бизнес-логику дуэлей
        return duelManager.joinOrCreateGame(gameCode, getPlayerId(playerDetails));
    }

    /**
     * Присоединяет текущего игрока к существующей игре по её коду.
     *
     * @param gameCode      уникальный код игры, к которой нужно присоединиться
     * @param playerDetails детали аутентифицированного игрока (через Spring Security)
     * @return сообщение о результате присоединения
     * (например, "Успешно присоединились" или "Комната полна")
     */
    @PostMapping("/JoinDuel")
    public String joinDuel(@RequestParam String gameCode,
                           @AuthenticationPrincipal PlayerDetails playerDetails) {
        // Передаём управление в бизнес-логику дуэлей
        return duelManager.joinGame(gameCode, getPlayerId(playerDetails));
    }

    /**
     * Создаёт новую дуэльную сессию с указанным кодом.
     *
     * @param gameCode      уникальный код для новой игры
     * @param playerDetails детали аутентифицированного игрока (создатель игры)
     * @return сообщение о результате создания игры
     * или текст ошибки, если игра с таким кодом уже существует
     */
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

    /**
     * Возвращает список всех активных дуэльных игр.
     *
     * @return список {@link GameSessionDto}, содержащий:
     * - gameCode: код игры,
     * - playersCount: текущее количество игроков в комнате
     */
    @GetMapping("/GetAllDuels")
    public List<GameSessionDto> getAllDuels() {
        duelManager.getAllDuels().forEach(d ->
                log.info("Game {} has {} players", d.gameCode(), d.playersCount())
        );
        return duelManager.getAllDuels();
    }

    private Long getPlayerId(PlayerDetails playerDetails) {
        return playerDetails.playerEntity().getId();
    }
}
