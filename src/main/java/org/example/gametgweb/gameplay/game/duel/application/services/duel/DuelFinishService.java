package org.example.gametgweb.gameplay.game.duel.application.services.duel;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.domain.repository.PlayerUnitRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.application.services.GameServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DuelFinishService — сервис для завершения дуэлей и обработки состояния юнитов.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>Сохранение состояния юнитов после боя;</li>
 *     <li>Сброс юнитов проигравших до шаблона;</li>
 *     <li>Удаление игровой сессии после окончания дуэли.</li>
 * </ul>
 */
@Slf4j
@Service
public class DuelFinishService {

    private final PlayerUnitRepositoryImpl playerUnitRepository;
    private final GameServiceImpl gameService;

    public DuelFinishService(PlayerUnitRepositoryImpl playerUnitRepository, GameServiceImpl gameService) {
        this.playerUnitRepository = playerUnitRepository;
        this.gameService = gameService;
    }

    /**
     * Завершает дуэль между двумя игроками.
     *
     * <p>Сохраняет состояние юнита победителя, ресетит юнит проигравшего до шаблона
     * и удаляет игровую сессию.</p>
     *
     * @param gameCode Игровая сессия дуэли
     * @param winner  Игрок-победитель
     * @param loser   Игрок-проигравший
     */
    @Transactional
    public void finishDuel(String gameCode, PlayerUnit winner, PlayerUnit loser) {

        Long winnerId = winner.getId();
        log.info("Winner id: {}", winnerId.toString());
        Long loserId = loser.getId();
        log.info("Winner id: {}", loserId.toString());

        // 1. сохранить состояние юнита победителя
        playerUnitRepository.save(winner);
        log.info(winner.getName() + " won the game");

        // 2. сбросить юнит проигравшего до шаблона и сохранить

        loser.resetToTemplate();
        playerUnitRepository.save(loser); // теперь это update
        log.info(loser.getName() + " lose the game");

        // 3. удалить комнату
        gameService.deleteByGameCode(gameCode);
    }

    /**
     * Завершает дуэль с одновременной смертью обоих игроков.
     *
     * <p>Ресетит юнитов обоих игроков до шаблона, сохраняет их состояния и удаляет игровую сессию.</p>
     *
     * @param gameCode Игровая сессия дуэли
     * @param loser1  Первый игрок, умерший в дуэли
     * @param loser2  Второй игрок, умерший в дуэли
     */
    @Transactional
    public void finishDuelWithDoubleDeath(String gameCode, PlayerUnit loser1, PlayerUnit loser2) {
        // Сбросить первого
            loser1.resetToTemplate();
            playerUnitRepository.save(loser1);

        // Сбросить второго
        loser2.resetToTemplate();
            playerUnitRepository.save(loser2);

        // 3. удалить комнату
        gameService.deleteByGameCode(gameCode);
    }
}

