package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
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
    private final ConcurrentHashMap<String, Map<String, PlayerUnit>> gameUnits = new ConcurrentHashMap<>();

    /**
     * Имена игроков по именам их юнитов
     * Key — String unit.getName(), Value — String playerName
     */
    private final ConcurrentHashMap<String, Map<String, String>> unitToPlayerMap = new ConcurrentHashMap<>();

    /**
     * Регистрирует юнита игрока в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @param unit     игровой юнит
     */
    public void registerUnit(String gameCode, String playerName, PlayerUnit unit) {
        gameUnits.computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>()).put(playerName, unit);
        unitToPlayerMap.computeIfAbsent(gameCode, k -> new ConcurrentHashMap<>())
                .put(unit.getName(), playerName);
        log.info("Юнит игрока {} (имя юнита {}) добавлен в комнату {}", playerName, unit.getName(), gameCode);
    }

    /**
     * Возвращает юнита игрока по имени в комнате.
     *
     * @param gameCode   код комнаты
     * @param playerName имя игрока
     * @return юнит игрока или null, если не найден
     */
    public PlayerUnit getUnit(String gameCode, String playerName) {
        PlayerUnit unit = gameUnits.getOrDefault(gameCode, new ConcurrentHashMap<>()).get(playerName);
        log.info("getUnit: {} в комнате {} -> {}", playerName, gameCode, unit != null ? "найден" : "не найден");
        return unit;
    }
    /**
     * Обновляет юнита игрока в комнате (например, после раунда боя).
     */
    public void updateUnit(String gameCode, String playerName, PlayerUnit updatedUnit) {
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
        PlayerUnit unit = gameUnits.getOrDefault(gameCode, Map.of()).remove(playerName);
        if (unit != null) {
            Map<String, String> unitMap = unitToPlayerMap.get(gameCode);
            if (unitMap != null) {
                unitMap.remove(unit.getName());
            }
        }
        log.info("Юнит {} удален из комнаты {}", playerName, gameCode);
    }


    public Map<String, PlayerUnit> getUnits(String gameCode) {
        return getGameUnits().getOrDefault(gameCode, Map.of());
    }

    public String resolvePlayer(String gameCode, PlayerUnit unit) {
        return getUnitToPlayerMap()
                .getOrDefault(gameCode, Map.of())
                .get(unit.getName());
    }
}
