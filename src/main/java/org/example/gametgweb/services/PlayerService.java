package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;

import java.util.Optional;

public interface PlayerService {
    Optional<PlayerEntity> findById(Long id);
    PlayerEntity savePlayer(PlayerEntity player);
    void deletePlayer(PlayerEntity player);
    PlayerEntity updatePlayer(PlayerEntity player);
    PlayerEntity setPlayer(PlayerEntity player);
    PlayerEntity findByUsername(String username);
}
