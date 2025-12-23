package org.example.gametgweb.gameplay.game.duel.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
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

    public Player getPlayerByName(String name) {
        return players.stream()
                .filter(p -> p.getUsername().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player " + name + " not found in session"));
    }

}
