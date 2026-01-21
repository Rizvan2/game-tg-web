package org.example.gametgweb.gameplay.game.duel.application.services.duel;

import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DuelTurnManager — менеджер текущих ходов дуэли для каждой игровой комнаты.
 *
 * <p>Хранит текущие ходы игроков по коду комнаты. Каждый ход представляет собой объект
 * {@link DuelTurn}, который содержит информацию о том, какие части тела выбрали игроки.
 * <p>
 * Основные задачи:
 * <ul>
 *     <li>Добавление хода игрока для текущей дуэли;</li>
 *     <li>Проверка готовности хода (когда оба игрока сделали выбор);</li>
 *     <li>Удаление завершённых или отменённых ходов после обработки.</li>
 * </ul>
 *
 * <p>Использует потокобезопасную коллекцию {@link ConcurrentHashMap} для хранения
 * текущих ходов по коду комнаты.
 */
@Component
public class DuelTurnManager {

    /**
     * Текущие ходы игроков по коду комнаты.
     * Key — gameCode, Value — DuelTurn (выборы игроков для текущего хода).
     */
    private final ConcurrentHashMap<String, DuelTurn> turns = new ConcurrentHashMap<>();

    /**
     * Добавляет ход игрока для указанной комнаты.
     * <p>
     * Если для комнаты ещё нет текущего хода, создаётся новый {@link DuelTurn}.
     * Затем игрок и его выбор добавляются в этот ход.
     *
     * @param gameCode код комнаты
     * @param player   имя игрока
     * @param body     выбранная часть тела
     * @return объект {@link DuelTurn} для текущего хода комнаты
     */
    public DuelTurn addMove(String gameCode, String player, Body body) {
        DuelTurn turn = turns.computeIfAbsent(gameCode, k -> new DuelTurn());
        turn.addMove(player, body);
        return turn;
    }

    /**
     * Удаляет текущий ход для указанной комнаты.
     * <p>
     * Используется после завершения дуэльного раунда, чтобы очистить состояние.
     *
     * @param gameCode код комнаты
     */
    public void removeTurn(String gameCode) {
        turns.remove(gameCode);
    }
}
