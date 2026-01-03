package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import org.example.gametgweb.gameplay.game.duel.application.services.GameSessionService;
import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервисный менеджер для управления созданием и присоединением игроков к дуэльным играм.
 * <p>
 * Обеспечивает единый API для контроллеров WebSocket или REST:
 * <ul>
 *     <li>Создание новой игры с первым игроком</li>
 *     <li>Присоединение игрока к существующей игре</li>
 *     <li>Формирование ссылки на страницу игры</li>
 * </ul>
 * <p>
 * Этот сервис служит "оркестратором", объединяя логику {@link GameSessionService} и возвращая готовые ссылки для фронтенда.
 */
@Service
public class DuelManager {

    private final GameSessionService gameService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param gameService сервис для работы с игровыми сессиями
     */
    @Autowired
    public DuelManager(GameSessionService gameService) {
        this.gameService = gameService;
    }

    /**
     * Присоединяет игрока к существующей игре с указанным {@code gameCode}
     * или создаёт новую игру, если её нет.
     * <p>
     * Логика метода:
     * <ol>
     *     <li>Проверка существования сессии с указанным {@code gameCode}</li>
     *     <li>Создание новой игры при отсутствии существующей</li>
     *     <li>Присоединение игрока к выбранной сессии</li>
     *     <li>Формирование ссылки на страницу игры</li>
     * </ol>
     *
     * @param gameCode код игры, по которому игрок подключается или создаётся игра
     * @param playerId ID игрока
     * @return ссылка на страницу дуэли, например "/duel-battle.html?id=ABCD1234"
     * @throws IllegalArgumentException если игрок с указанным ID не найден
     */
    public String joinOrCreateGame(String gameCode, Long playerId) {
        return buildGameLink(gameService.joinOrCreateGame(gameCode, playerId));
    }

    /**
     * Создаёт новую игру с указанным {@code gameCode} и сразу прикрепляет игрока.
     * <p>
     * Если игра с таким кодом уже существует, будет выброшено исключение {@link org.example.gametgweb.gameplay.game.duel.domain.exception.GameAlreadyExistsException}.
     *
     * @param gameCode код новой игры
     * @param playerId ID игрока
     * @return ссылка на страницу игры
     */
    public String createGame(String gameCode, Long playerId) {
        return buildGameLink(gameService.createGameAndAttachPlayer(gameCode, playerId));
    }

    /**
     * Присоединяет игрока к существующей игре.
     * <p>
     * Если игра с указанным {@code gameCode} не найдена, будет выброшено исключение {@link IllegalArgumentException}.
     *
     * @param gameCode код существующей игры
     * @param playerId ID игрока
     * @return ссылка на страницу игры
     */
    public String joinGame(String gameCode, Long playerId) {
        return buildGameLink(gameService.joinGame(gameCode, playerId));
    }

    /**
     * Формирует URL ссылки на страницу дуэли по доменной модели {@link GameSession}.
     * <p>
     * Используется {@link GameSession#getGameCode()} для отображения "имени комнаты" в URL,
     * а не ID базы данных.
     *
     * @param game объект игрового сеанса
     * @return ссылка вида "/duel-battle.html?id={gameCode}"
     */
    private String buildGameLink(GameSession game) {
        return "/duel-battle.html?id=" + game.getGameCode();
    }
}

