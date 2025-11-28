package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

public record InfoMessageDTO(String type, String message) {
    public InfoMessageDTO(String message) {
        this("info", message); // вызывает основной конструктор
    }
}
