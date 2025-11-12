package org.example.gametgweb.gameplay.game.duel.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;

public class GameSession {

    private Long id;
    private String gameCode;
    private GameState state;
    private List<Player> players = new ArrayList<>();

    public GameSession(String gameCode, GameState state) {
        this.gameCode = gameCode;
        this.state = state;
    }
    public GameSession(Long id, String gameCode, GameState state) {
        this.id = id;
        this.gameCode = gameCode;
        this.state = state;
    }
    public GameSession(Long id, String gameCode, GameState state, List<Player> players) {
        this.id = id;
        this.gameCode = gameCode;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
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
