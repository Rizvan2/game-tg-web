package org.example.gametgweb.characterSelection.dto;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;

public record PlayerResponse(
        Long id,
        String username,
        Long activeUnitId
) {
    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getActiveUnit() != null ? player.getActiveUnit().getId() : null
        );
    }
}
