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
    private final Map<String, Body> moves = new HashMap<>();
    private final Map<String, Boolean> readyFlags = new HashMap<>(); // игрок нажал "Атаковать"
    private boolean bothSelectedNotified = false;

    public void addMove(String player, Body body) {
        moves.put(player, body);
    }

    public void setReady(String player) {
        readyFlags.put(player, true);
    }

    public boolean isReady() {
        return moves.size() == 2 && readyFlags.size() == 2;
    }

    public boolean isBothSelectedNotified() {
        return bothSelectedNotified;
    }

    public void setBothSelectedNotified(boolean val) {
        this.bothSelectedNotified = val;
    }

    public String getPlayer1() { return new ArrayList<>(moves.keySet()).get(0); }
    public String getPlayer2() { return new ArrayList<>(moves.keySet()).get(1); }
    public Body getBody1() { return moves.get(getPlayer1()); }
    public Body getBody2() { return moves.get(getPlayer2()); }
}

