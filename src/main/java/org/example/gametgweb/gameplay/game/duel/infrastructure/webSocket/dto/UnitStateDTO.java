package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record UnitStateDTO(
        Long playerId,
        String player,
        String unitName,
        long hp,
        long hpMax,
        String imagePath
) {}
