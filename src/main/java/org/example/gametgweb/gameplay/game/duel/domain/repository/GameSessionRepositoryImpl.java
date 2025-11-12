package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.GameSessionMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class GameSessionRepositoryImpl implements GameSessionRepository {

    private final JpaGameSessionRepository jpaRepository;

    public GameSessionRepositoryImpl(JpaGameSessionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<GameSession> findByCode(String code) {
        return jpaRepository.findByGameCode(code)
                .map(GameSessionMapper::toDomain);
    }

    @Override
    public GameSession save(GameSession game) {
        GameSessionEntity entity = GameSessionMapper.toEntity(game);
        return GameSessionMapper.toDomain(jpaRepository.save(entity));
    }
}
