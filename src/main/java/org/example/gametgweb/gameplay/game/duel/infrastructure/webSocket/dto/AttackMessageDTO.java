package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttackMessageDTO(String type, String body) {
    @JsonCreator
    public AttackMessageDTO(@JsonProperty("body") String body) {
        this("attack", body);
    }

    public Body bodyEnum() {
        if (body == null) return null;
        try {
            return Body.valueOf(body.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}