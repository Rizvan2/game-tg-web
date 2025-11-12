package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    private final JpaPlayerRepository playerRepository;

    public PlayerRepositoryImpl(JpaPlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id)
                .map(PlayerMapper::mapPlayerToDomain);
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        return playerRepository.findByUsername(username)
                .map(PlayerMapper::mapPlayerToDomain);
    }

    @Override
    public List<Player> findAll() {
        return playerRepository.findAll().stream()
                .map(PlayerMapper::mapPlayerToDomain)
                .toList();
    }

    @Override
    public Player save(Player player) {
        return PlayerMapper.mapPlayerToDomain(
                playerRepository.save(PlayerMapper.mapPlayerToEntity(player, null)) // gameSession можно передавать отдельно
        );
    }

    @Override
    public void deleteById(Long id) {
        playerRepository.deleteById(id);
    }
}

