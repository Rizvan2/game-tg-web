package org.example.gametgweb.gameplay.game.entity.gameSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.example.gametgweb.gameplay.game.entity.player.Player;

public class GameSession {

    private Long id;
    private String gameCode;
    private GameState state;
    private final List<Player> players = new ArrayList<>();

    public GameSession(Long id, String gameCode, GameState state) {
        this.id = id;
        this.gameCode = gameCode;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public String getGameCode() {
        return gameCode;
    }

    public GameState getState() {
        return state;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void addPlayer(Player player) {
        if (state != GameState.WAITING) {
            throw new IllegalStateException("Cannot join: game already started or finished");
        }
        this.players.add(player);
    }

    public void start() {
        if (players.size() < 2) {
            throw new IllegalStateException("Cannot start: not enough players");
        }
        this.state = GameState.IN_PROGRESS;
    }

    public void finish() {
        this.state = GameState.FINISHED;
    }
}
