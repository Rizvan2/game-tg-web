package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record DuelRoundResponseDto(
        String attacker,
        String defender,
        String[] turnMessages,
        double attackerHp,
        double defenderHp
) {}
