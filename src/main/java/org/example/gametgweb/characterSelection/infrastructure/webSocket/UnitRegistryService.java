package org.example.gametgweb.characterSelection.infrastructure.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.RoomSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UnitRegistryService {

    private final RoomSessionRegistry registry;

    @Autowired
    public UnitRegistryService(RoomSessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Регистрирует юнита игрока в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @param unit       юнит игрока
     */
    public void registerUnit(String gameCode, String playerName, Unit unit) {
        registry.registerUnit(gameCode, playerName, unit);
        log.info("Юнит {} зарегистрирован для игрока {} в комнате {}", unit.getName(), playerName, gameCode);
    }


    /**
     * Возвращает юнита игрока по имени в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return юнит игрока или null
     */
    public Unit getUnit(String gameCode, String playerName) {
        Unit unit = registry.getUnit(gameCode, playerName);
        if (unit != null) {
            log.debug("Юнит {} найден в комнате {}", playerName, gameCode);
        } else {
            log.debug("Юнит {} не найден в комнате {}", playerName, gameCode);
        }
        return unit;
    }

    /**
     * Обновляет юнита игрока в комнате (например, после раунда боя).
     */
    public void updateUnit(String gameCode, String playerName, Unit updatedUnit) {
        registry.registerUnit(gameCode, playerName, updatedUnit);
        log.info("Юнит игрока {} обновлен в комнате {}", playerName, gameCode);
    }

    /**
     * Удаляет юнита игрока из комнаты (например, при выходе).
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     */
    public void removeUnit(String gameCode, String playerName) {
        registry.getUnit(gameCode, playerName); // для будущей логики, например очистка
        log.info("Юнит {} удален из комнаты {}", playerName, gameCode);
    }
}
