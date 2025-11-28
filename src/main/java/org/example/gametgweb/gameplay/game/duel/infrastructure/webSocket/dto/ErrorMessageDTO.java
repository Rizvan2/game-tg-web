package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record ErrorMessageDTO(String type, String message) {
    public ErrorMessageDTO(String message) {
        this("error", message);
    }
}
