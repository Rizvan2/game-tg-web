package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record InitMessageDTO(
    String type,
    String playerName,
    String playerUnitName
) {}
