package org.example.gametgweb.gameplay.game.duel.application.events;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;

public record DuelDrawEvent(
        String gameCode,
        PlayerUnit loser1,
        PlayerUnit loser2,
        String player1Name,
        String player2Name
) {}