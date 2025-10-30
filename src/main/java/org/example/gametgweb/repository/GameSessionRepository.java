package org.example.gametgweb.repository;

import org.example.gametgweb.gameplay.game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession getGameById(Long id);

    Optional<GameSession> findGameByGameCode(String gameCode);
}
