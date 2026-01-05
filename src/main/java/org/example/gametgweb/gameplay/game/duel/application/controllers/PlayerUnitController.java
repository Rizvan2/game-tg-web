package org.example.gametgweb.gameplay.game.duel.application.controllers;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.api.dto.PlayerUnitDto;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для работы с активным игровым юнитом текущего игрока.
 * <p>
 * Позволяет:
 * - Получать информацию о текущем юните игрока,
 * включая имя, изображение, здоровье и урон.
 * <p>
 * Использует {@link PlayerRepositoryImpl} для доступа к данным игрока.
 */
@RestController
public class PlayerUnitController {

    private final PlayerRepositoryImpl playerRepository;

    @Autowired
    public PlayerUnitController(PlayerRepositoryImpl playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Возвращает DTO активного юнита текущего игрока.
     *
     * @param playerDetails аутентифицированный игрок (через Spring Security)
     * @return {@link PlayerUnitDto} с данными юнита:
     * - имя
     * - путь к изображению
     * - текущее здоровье
     * - максимальное здоровье
     * - урон
     * @throws IllegalArgumentException если игрок или юнит не найдены
     */
    @GetMapping("/GetPlayerUnit")
    public PlayerUnitDto getPlayerUnit(@AuthenticationPrincipal PlayerDetails playerDetails) {
        // Берём актуального игрока из базы
        Player freshPlayer = playerRepository.findById(playerDetails.playerEntity().getId())
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        PlayerUnit playerUnit = freshPlayer.getActiveUnit()
                .orElseThrow(() -> new IllegalArgumentException("Юнит не найден"));

        return new PlayerUnitDto(
                playerUnit.getName(),
                playerUnit.getImagePath(),
                playerUnit.getHealth(),
                playerUnit.getMaxHealth(),
                playerUnit.getDamage()
        );
    }
}