package org.example.gametgweb.characterSelection.application.services;

import org.example.gametgweb.characterSelection.api.dto.SelectUnitRequest;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.domain.repository.PlayerUnitRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.application.services.PlayerUnitSelectionService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис, отвечающий за бизнес-логику выбора персонажа (юнита) игроком.
 * <p>
 * Взаимодействует с репозиториями для поиска юнитов и обновления данных игрока.
 */
@Service
public class CharacterSelectionService {

    private final PlayerUnitRepositoryImpl unitRepository;
    private final PlayerUnitSelectionService playerUnitSelectionService;

    /**
     * Конструктор для внедрения зависимостей (репозиториев).
     *
     * @param unitRepository Репозиторий для доступа к данным юнитов.
     */
    @Autowired
    public CharacterSelectionService(PlayerUnitRepositoryImpl unitRepository, PlayerUnitSelectionService playerUnitSelectionService) {
        this.unitRepository = unitRepository;
        this.playerUnitSelectionService = playerUnitSelectionService;
    }


    @Transactional
    public Player selectUnitForPlayer(SelectUnitRequest request, PlayerDetails playerDetails) {
        // Преобразование сущности игрока из Spring Security в доменную модель
        Player player = PlayerMapper.toDomain(playerDetails.playerEntity());

        // 1. Ищем юнита
        PlayerUnit unit = unitRepository.findByName(request.unitName())
                .orElseThrow(() -> new IllegalArgumentException("Юнит с таким именем не найден: " + request.unitName()));

        return playerUnitSelectionService.selectUnitForPlayer(player, unit);
    }
}