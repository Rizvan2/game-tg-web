package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;

import java.util.Optional;

public interface PlayerService {
    Optional<PlayerEntity> getPlayer(Long id);
    PlayerEntity savePlayer(PlayerEntity player);
    void deletePlayer(PlayerEntity player);
    PlayerEntity updatePlayer(PlayerEntity player);
    PlayerEntity setPlayer(PlayerEntity player);
}
