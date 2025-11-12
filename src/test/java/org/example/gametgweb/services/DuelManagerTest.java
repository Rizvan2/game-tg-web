package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.duel.application.services.PlayerServiceImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;
import org.example.gametgweb.gameplay.game.duel.application.services.GameServiceImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class DuelManagerTest {
    @Mock
    private GameServiceImpl gameService;

    @Mock
    private JpaGameSessionRepository jpaGameSessionRepository;

    @Mock
    private PlayerServiceImpl playerService;

    @InjectMocks
    private DuelManager duelManager;

    @Test
    void joinOrCreateGame() {
        String gameCode = "нога";
        Long playerId = 1L;
        GameSessionEntity game = new GameSessionEntity();
        game.setGameCode(gameCode);
        game.setState(GameState.WAITING);
        when(gameService.createGame(gameCode, playerId)).thenReturn(game);
        when(playerService.findById(playerId)).thenReturn(Optional.of(new PlayerEntity("123","Артьом"
        ,new UnitEntity(4L,"Goblin", 100L, 100L,10L,"/images/Goblin.png"))));
        String link = duelManager.joinOrCreateGame(gameCode,playerId);
        verify(gameService, times(1)).createGame(gameCode, playerId);
        // Проверяем результат
        assertEquals("/gameplay.html?id=" + game.getId(), link);
    }

    @Test
    void findGameByGameCode() {
    }
}