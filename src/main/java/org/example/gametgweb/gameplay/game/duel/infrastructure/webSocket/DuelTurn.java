package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DuelTurn — представляет текущий ход дуэли между двумя игроками.
 *
 * <p>Хранит выборы игроков (части тела для атаки/блокирования) и позволяет
 * определить, когда оба игрока сделали свой ход.
 *
 * <p>Используется {@link DuelTurnManager} для управления ходами по каждой игровой комнате.
 */
public class DuelTurn {

    /** Ходы игроков: ключ — имя игрока, значение — выбранная часть тела. */
    private final Map<String, Body> moves = new HashMap<>();

    /**
     * Добавляет ход игрока.
     * <p>Если игрок уже сделал выбор, он перезаписывается.
     *
     * @param player имя игрока
     * @param body   выбранная часть тела
     */
    public void addMove(String player, Body body) {
        moves.put(player, body);
    }

    /**
     * Проверяет, завершили ли оба игрока свои ходы.
     *
     * @return {@code true}, если сделано два хода, иначе {@code false}
     */
    public boolean isReady() {
        return moves.size() == 2;
    }

    /**
     * Возвращает имя первого игрока, сделавшего ход.
     * <p>Порядок извлечения берется из ключей {@link HashMap}.
     *
     * @return имя первого игрока
     */
    public String getPlayer1() {
        return new ArrayList<>(moves.keySet()).get(0);
    }

    /**
     * Возвращает имя второго игрока, сделавшего ход.
     * <p>Порядок извлечения берется из ключей {@link HashMap}.
     *
     * @return имя второго игрока
     */
    public String getPlayer2() {
        return new ArrayList<>(moves.keySet()).get(1);
    }

    /**
     * Возвращает выбор (часть тела) первого игрока.
     *
     * @return выбранная часть тела первого игрока
     */
    public Body getBody1() {
        return moves.get(getPlayer1());
    }

    /**
     * Возвращает выбор (часть тела) второго игрока.
     *
     * @return выбранная часть тела второго игрока
     */
    public Body getBody2() {
        return moves.get(getPlayer2());
    }
}
