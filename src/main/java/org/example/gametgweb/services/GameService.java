package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.GameSession;

public interface GameService {
    GameSession findGameByGameCode(String gameCode);
    void setGame(GameSession game);
    void deleteGame(Long id);
    GameSession createGame(String gameCode, Long playerId);
    void updateGame(GameSession game);
    GameSession getOrCreateGame(String gameCode);
}
