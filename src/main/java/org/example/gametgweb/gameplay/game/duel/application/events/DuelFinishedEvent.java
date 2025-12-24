package org.example.gametgweb.gameplay.game.duel.application.events;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;

public record DuelFinishedEvent(
        String gameCode,
        PlayerUnit winner,
        PlayerUnit loser
) {}