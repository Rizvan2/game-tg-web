package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
@Service
public class UnitRegistryService {

    /**
     * Игровые юниты игроков, сгруппированные по коду комнаты.
     * Key — gameCode, Value — Map playerName -> Unit
     */
    private final ConcurrentHashMap<String, Map<String, Unit>> gameUnits = new ConcurrentHashMap<>();

    /**
     * Регистрирует юнита игрока в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @param unit     игровой юнит
     */
    public void registerUnit(String gameCode, String playerName, Unit unit) {
        gameUnits.computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>()).put(playerName, unit);
        log.info("Юнит игрока {} (имя юнита {}) добавлен в комнату {}", playerName, unit.getName(), gameCode);
    }

    /**
     * Возвращает юнита игрока по имени в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return юнит игрока или null, если не найден
     */
    public Unit getUnit(String gameCode, String playerName) {
        Unit unit = gameUnits.getOrDefault(gameCode, new ConcurrentHashMap<>()).get(playerName);
        log.info("getUnit: {} в комнате {} -> {}", playerName, gameCode, unit != null ? "найден" : "не найден");
        return unit;
    }
    /**
     * Обновляет юнита игрока в комнате (например, после раунда боя).
     */
    public void updateUnit(String gameCode, String playerName, Unit updatedUnit) {
        registerUnit(gameCode, playerName, updatedUnit);
        log.info("Юнит игрока {} обновлен в комнате {}", playerName, gameCode);
    }

    /**
     * Удаляет юнита игрока из комнаты (например, при выходе).
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     */
    public void removeUnit(String gameCode, String playerName) {
        getUnit(gameCode, playerName); // для будущей логики, например очистка
        log.info("Юнит {} удален из комнаты {}", playerName, gameCode);
    }

    public Map<String, Unit> getUnits(String gameCode) {
        return getGameUnits().getOrDefault(gameCode, Map.of());
    }
}
