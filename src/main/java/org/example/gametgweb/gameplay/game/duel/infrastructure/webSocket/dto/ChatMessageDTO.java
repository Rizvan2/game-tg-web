package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;

    @JsonProperty("playerName")
    private String playerName;

    @JsonProperty("sender")
    private String sender;

    // Пустой конструктор для Jackson
    public ChatMessageDTO() {}

    // Конструктор для создания сообщений в коде
    public ChatMessageDTO(String playerName, String message) {
        this.type = "chat";
        this.playerName = playerName;
        this.message = message;
    }

    public String getEffectiveSender() {
        return playerName != null ? playerName : sender;
    }
}
