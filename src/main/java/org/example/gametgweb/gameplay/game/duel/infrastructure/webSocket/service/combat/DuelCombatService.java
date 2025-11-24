package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelTurnManager;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * DuelCombatService — сервис обработки логики боя между двумя игроками в дуэли.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>Добавление хода игрока через {@link DuelTurnManager};</li>
 *     <li>Определение готовности хода (когда оба игрока сделали выбор);</li>
 *     <li>Вызов {@link CombatService#duelRound(Unit, Body, Unit, Body)} для расчёта результатов боя;</li>
 *     <li>Очистку хода после завершения раунда;</li>
 *     <li>Возврат результата боя в виде JSON строки.</li>
 * </ul>
 */
@Component
public class DuelCombatService {

    private final DuelTurnManager turnManager;
    private final CombatService combatService;
    private final RoomSessionRegistry roomSessionRegistry;

    @Autowired
    public DuelCombatService(DuelTurnManager turnManager, CombatService combatService, RoomSessionRegistry roomSessionRegistry) {
        this.turnManager = turnManager;
        this.combatService = combatService;
        this.roomSessionRegistry = roomSessionRegistry;
    }

    /**
     * Обрабатывает атаку игрока в текущем раунде дуэли.
     *
     * <p>Метод:
     * <ol>
     *     <li>Добавляет ход игрока через {@link DuelTurnManager#addMove(String, String, Body)};</li>
     *     <li>Проверяет, готовы ли оба игрока;</li>
     *     <li>Если оба игрока сделали выбор, получает их юниты из {@link RoomSessionRegistry};</li>
     *     <li>Вызывает {@link CombatService#duelRound(Unit, Body, Unit, Body)} для расчёта результата;</li>
     *     <li>Удаляет текущий ход из менеджера ходов;</li>
     *     <li>Возвращает результат в виде JSON строки.</li>
     * </ol>
     *
     * @param gameCode код комнаты дуэли
     * @param player   имя игрока, делающего ход
     * @param body     выбранная часть тела для атаки/блока
     * @return JSON строка с результатом боя, либо {@code null}, если оба игрока ещё не сделали выбор
     * @throws Exception если произошла ошибка сериализации JSON
     */
    public String processAttack(String gameCode, String player, Body body) throws Exception {
        var turn = turnManager.addMove(gameCode, player, body);

        // игрок нажал "Атаковать"
        turn.setReady(player);

        // уведомление о том, что оба игрока сделали выбор
        if (turn.isReady() && !turn.isBothSelectedNotified()) {
            turn.setBothSelectedNotified(true);
            roomSessionRegistry.broadcast(
                    gameCode,
                    new ObjectMapper().writeValueAsString(Map.of("type", "bothSelected"))
            );
        }

        // если оба игрока нажали "Атаковать" → считаем раунд
        if (turn.isReady()) {
            Unit unitEntity1 = roomSessionRegistry.getUnit(gameCode, turn.getPlayer1());
            Unit unitEntity2 = roomSessionRegistry.getUnit(gameCode, turn.getPlayer2());

            var result = combatService.duelRound(unitEntity1, turn.getBody1(), unitEntity2, turn.getBody2());

// Добавляем явные поля для фронта
            Map<String, Object> resultWithPlayers = new HashMap<>(result);
            resultWithPlayers.put("attacker", unitEntity1.getName());
            resultWithPlayers.put("defender", unitEntity2.getName());

// очищаем ход после раунда
            turnManager.removeTurn(gameCode);

            return new ObjectMapper().writeValueAsString(resultWithPlayers);

        }

        return null; // ждём второго игрока
    }
}
