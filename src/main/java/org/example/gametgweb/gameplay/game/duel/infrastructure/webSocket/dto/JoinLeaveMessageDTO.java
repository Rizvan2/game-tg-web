package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record JoinLeaveMessageDTO(String type, String playerName, String gameCode, String message) {
}
