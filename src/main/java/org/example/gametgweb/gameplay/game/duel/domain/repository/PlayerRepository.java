package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    Optional<Player> findById(Long id);

    Optional<Player> findByUsername(String username);

    List<Player> findAll();

    Player save(Player player);

    void deleteById(Long id);
}
