package org.example.gametgweb.characterSelection.services;

import org.example.gametgweb.characterSelection.dto.SelectUnitRequest;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.domain.repository.UnitRepositoryImpl;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.shared.PlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис, отвечающий за бизнес-логику выбора персонажа (юнита) игроком.
 * <p>
 * Взаимодействует с репозиториями для поиска юнитов и обновления данных игрока.
 */
@Service
public class CharacterSelectionService {

    private final UnitRepositoryImpl unitRepository;
    private final PlayerRepositoryImpl playerRepository;

    /**
     * Конструктор для внедрения зависимостей (репозиториев).
     *
     * @param unitRepository Репозиторий для доступа к данным юнитов.
     * @param playerRepository Репозиторий для доступа и сохранения данных игрока.
     */
    @Autowired
    public CharacterSelectionService(UnitRepositoryImpl unitRepository, PlayerRepositoryImpl playerRepository) {
        this.unitRepository = unitRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Выполняет выбор и сохранение активного юнита для аутентифицированного игрока.
     *
     * <p>Этот метод инкапсулирует бизнес-логику:
     * <ol>
     * <li>Преобразование данных аутентификации в доменную модель Player.</li>
     * <li>Поиск выбранного юнита по его имени.</li>
     * <li>Установка найденного юнита в качестве активного юнита игрока.</li>
     * <li>Сохранение обновленной доменной модели Player.</li>
     * </ol>
     *
     * @param request Объект запроса, содержащий имя выбранного юнита (unitName).
     * @param playerDetails Данные аутентифицированного пользователя, полученные из Spring Security.
     * @return Обновленная доменная модель {@link Player} с установленным активным юнитом.
     * @throws IllegalArgumentException если юнит с указанным именем не найден в базе данных.
     */
    @Transactional
    public Player selectUnitForPlayer(SelectUnitRequest request, PlayerDetails playerDetails) {
        // Преобразование сущности игрока из Spring Security в доменную модель
        Player player = PlayerMapper.toDomain(playerDetails.playerEntity());

        // 1. Ищем юнита
        Unit unit = unitRepository.findByName(request.unitName())
                .orElseThrow(() -> new IllegalArgumentException("Юнит с таким именем не найден: " + request.unitName()));

        // 2. Ставим выбранного юнита игроку
        player.setActiveUnit(unit);

        // 3. Сохраняем игрока
        return playerRepository.savePlayer(player);
    }

    /**
     * Возвращает список всех игровых юнитов
     * @return Все игровые юниты
     */
    @Transactional(readOnly = true)
    public List<Unit> getAllActiveUnits() {
        return unitRepository.findAll();
    }
}