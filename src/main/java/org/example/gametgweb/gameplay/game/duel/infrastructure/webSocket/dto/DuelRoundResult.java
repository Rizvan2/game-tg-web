package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record DuelRoundResult(
        String[] turnMessages,
        long attackerHp,
        long defenderHp
) {
    public boolean attackerDead() {
        return attackerHp <= 0;
    }

    public boolean defenderDead() {
        return defenderHp <= 0;
    }
}
