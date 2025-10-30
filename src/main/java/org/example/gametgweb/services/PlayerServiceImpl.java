package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<PlayerEntity> getPlayer(Long id) {
        return playerRepository.findById(id);
    }

    @Override
    public PlayerEntity savePlayer(PlayerEntity player) {
        return playerRepository.save(player);
    }

    @Override
    public void deletePlayer(PlayerEntity player) {
        playerRepository.delete(player);
    }

    @Override
    public PlayerEntity updatePlayer(PlayerEntity player) {
        return null;
    }

    @Override
    public PlayerEntity setPlayer(PlayerEntity player) {
        return null;
    }
}
