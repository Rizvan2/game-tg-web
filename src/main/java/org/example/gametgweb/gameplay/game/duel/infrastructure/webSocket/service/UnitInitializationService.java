package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UnitInitializationService — Сервис, отвечающий за бизнес-логику инициализации
 * игрового юнита при первом подключении игрока к комнате.
 *
 * <p>Сервис выполняет:
 * <ul>
 * <li>Загрузку сущности игрока из базы данных (через {@link PlayerRepositoryImpl});</li>
 * <li>Проверку наличия активного юнита;</li>
 * <li>Регистрацию активного юнита в реестре текущей игры (через {@link UnitRegistryService}).</li>
 * </ul>
 * Это позволяет отделить логику доступа к данным и регистрации юнитов от логики управления WebSocket-сессиями.</p>
 */
@Component
@Slf4j
public class UnitInitializationService {

    private final PlayerRepositoryImpl playerService;
    private final UnitRegistryService unitRegistry;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param playerService Репозиторий для доступа к данным игрока ({@link Player} Entity).
     * @param unitRegistry  Сервис для регистрации и хранения активных юнитов в контексте игры.
     */
    @Autowired
    public UnitInitializationService(PlayerRepositoryImpl playerService, UnitRegistryService unitRegistry) {
        this.playerService = playerService;
        this.unitRegistry = unitRegistry;
    }

    /**
     * Загружает данные игрока и регистрирует его активного юнита в реестре текущей дуэли.
     *
     * <p>Если игрок или его активный юнит не найден, регистрация не выполняется,
     * но ошибки не генерируются, так как это может быть связано с неполными данными игрока.</p>
     *
     * @param gameCode   Код комнаты, в которой регистрируется юнит.
     * @param playerName Имя игрока, чей юнит регистрируется.
     */
    public void handleNewJoin(String gameCode, String playerName) {
        Player playerEntity = playerService.findByUsername(playerName);
        if (playerEntity != null && playerEntity.getActiveUnit() != null) {
            unitRegistry.registerUnit(gameCode, playerName, playerEntity.getActiveUnit());
            log.info("Юнит {} зарегистрирован для игрока {} в комнате {}",
                    playerEntity.getActiveUnit().getName(), playerName, gameCode);
        }
    }
}
