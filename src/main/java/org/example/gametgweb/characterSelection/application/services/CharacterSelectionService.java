package org.example.gametgweb.characterSelection.application.services;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.api.dto.SelectUnitRequest;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.domain.repository.PlayerUnitRepositoryImpl;
import org.example.gametgweb.characterSelection.domain.repository.UnitRepositoryImpl;
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
@Slf4j
@Service
public class CharacterSelectionService {

    private final UnitRepositoryImpl unitRepository;
    private final PlayerUnitRepositoryImpl playerUnitRepository;
    private final PlayerUnitSelectionService playerUnitSelectionService;

    /**
     * Конструктор для внедрения зависимостей (репозиториев).
     *
     * @param unitRepository Репозиторий для доступа к данным юнитов.
     */
    @Autowired
    public CharacterSelectionService(UnitRepositoryImpl unitRepository, PlayerUnitRepositoryImpl playerUnitRepository, PlayerUnitSelectionService playerUnitSelectionService) {
        this.unitRepository = unitRepository;
        this.playerUnitRepository = playerUnitRepository;
        this.playerUnitSelectionService = playerUnitSelectionService;
    }


    @Transactional
    public Player selectUnitForPlayer(SelectUnitRequest request, PlayerDetails playerDetails) {
        log.info(request.customUnitName(), request.unitName());
        Player player = PlayerMapper.toDomain(playerDetails.playerEntity());

        Unit templateUnit = unitRepository.findByName(request.unitName())
                .orElseThrow(() -> new IllegalArgumentException("Юнит с таким именем не найден: " + request.unitName()));

        PlayerUnit unit = new PlayerUnit(templateUnit, request.customUnitName()); // копируем все поля


        // Сохраняем и используем объект с присвоенным id
        PlayerUnit savedUnit = playerUnitRepository.save(unit);
        log.info(savedUnit.getId().toString());

        return playerUnitSelectionService.selectUnitForPlayer(player, savedUnit);
    }

}