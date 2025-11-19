package org.example.gametgweb.characterSelection.controllers;

import org.example.gametgweb.characterSelection.dto.PlayerResponse;
import org.example.gametgweb.characterSelection.dto.SelectUnitRequest;
import org.example.gametgweb.characterSelection.services.CharacterSelectionService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер, отвечающий за операции выбора игрового юнита.
 * <p>
 * Предоставляет эндпоинты для:
 * <ul>
 *     <li>Получения списка доступных юнитов для выбора</li>
 *     <li>Установки выбранного юнита текущему игроку</li>
 * </ul>
 * <p>
 * Контроллер не содержит бизнес-логики: он делегирует операции
 * сервису {@link CharacterSelectionService} и формирует корректный HTTP-ответ.
 */
@RestController
@RequestMapping("/units")
public class UnitSelectionController {

    private final CharacterSelectionService selectionService;

    public UnitSelectionController(CharacterSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    /**
     * Устанавливает выбранного юнита текущему игроку.
     * <p>
     * Эндпоинт принимает DTO с именем юнита и назначает найденного юнита
     * активным для авторизованного игрока.
     *
     * @param request DTO с именем юнита для выбора
     * @param user Текущий аутентифицированный пользователь
     * @return DTO {@link PlayerResponse} с обновлённым состоянием игрока
     *
     * @apiNote
     * Требует авторизации. Доменный объект Player не возвращается напрямую —
     * используется DTO-обёртка для защиты модели.
     *
     * Пример запроса:
     * <pre>
     * POST /units/select
     * {
     *   "unitName": "Warrior"
     * }
     * </pre>
     */
    @PostMapping("/select")
    public ResponseEntity<PlayerResponse> selectUnit(
            @RequestBody SelectUnitRequest request,
            @AuthenticationPrincipal PlayerDetails user
    ) {
        Player player = selectionService.selectUnitForPlayer(request, user);
        return ResponseEntity.ok(PlayerResponse.from(player));
    }

    /**
     * Возвращает список всех доступных игровых юнитов.
     * <p>
     * Используется фронтендом для отображения окна выбора персонажа.
     *
     * @return Список всех доступных юнитов
     *
     * @apiNote
     * Эндпоинт открыт для всех авторизованных пользователей.
     *
     * Пример запроса:
     * <pre>
     * GET /units
     * </pre>
     */
    @GetMapping
    public ResponseEntity<List<Unit>> getAllUnits() {
        return ResponseEntity.ok(selectionService.getAllActiveUnits());
    }
}
