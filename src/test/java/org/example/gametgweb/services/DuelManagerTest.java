//package org.example.gametgweb.services;
//
//import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
//import org.example.gametgweb.gameplay.game.duel.domain.repository.GameSessionRepositoryImpl;
//import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
//import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelManager;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.*;
//@ExtendWith(MockitoExtension.class)
//class DuelManagerTest {
//    @Mock
//    private GameSessionRepositoryImpl gameService;
//
//    @Mock
//    private PlayerRepositoryImpl playerService;
//
//    @InjectMocks
//    private DuelManager duelManager;
//
//    @Test
//    void joinOrCreateGame_returnsCorrectLink() {
//        // Подготовка
//        String gameCode = "нога";
//        Long playerId = 1L;
//
//        GameSession game = new GameSession(123L, gameCode, null);
//
//        GameSessionRepositoryImpl gameService = mock(GameSessionRepositoryImpl.class);
//
//        DuelManager duelManager = new DuelManager(gameService);
//
//        when(gameService.joinOrCreateGame(gameCode, playerId)).thenReturn(game);
//
//        // Вызов тестируемого метода
//        String link = duelManager.joinOrCreateGame(gameCode, playerId);
//
//        // Проверка вызова репозитория
//        verify(gameService, times(1)).joinOrCreateGame(gameCode, playerId);
//        verifyNoInteractions(playerService); // DuelManager не работает с игроками напрямую
//
//        // Проверка результата
//        assertEquals("/duel-battle.html?id=123", link);
//    }
//
//
//    @Test
//    void findGameByGameCode() {
//    }
//}