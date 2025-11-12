package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;

import java.util.Optional;

public interface GameSessionRepository {

    Optional<GameSession> findByCode(String code);

    GameSession save(GameSession game);
}
