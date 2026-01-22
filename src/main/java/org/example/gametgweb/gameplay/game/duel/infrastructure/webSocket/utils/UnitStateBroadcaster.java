package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.UnitStateDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.UnitsStateMessageDTO;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.session.SessionRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.session.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.MessageDispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Сервис, отвечающий за сбор актуального состояния игровых юнитов
 * и рассылку этого состояния всем активным WebSocket-сессиям,
 * связанным с определенной игровой сессией (gameCode).
 *
 * Использует UnitRegistryService для получения данных юнитов,
 * SessionRegistryService для получения списка сессий и
 * MessageDispatcherService для фактической отправки сообщений.
 */
@Component
@Slf4j
public class UnitStateBroadcaster {

    private final UnitRegistryService unitRegistry;
    private final SessionRegistryService sessionRegistry;
    private final MessageDispatcherService messageDispatcher;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param unitRegistry Сервис для доступа к данным юнитов в игре.
     * @param sessionRegistry Сервис для управления и получения списка активных WebSocket-сессий.
     * @param messageDispatcher Сервис для отправки сообщений в конкретные WebSocket-сессии.
     */
    @Autowired
    public UnitStateBroadcaster(UnitRegistryService unitRegistry,
                                SessionRegistryService sessionRegistry,
                                MessageDispatcherService messageDispatcher) {
        this.unitRegistry = unitRegistry;
        this.sessionRegistry = sessionRegistry;
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * Главный метод для сбора состояния всех юнитов в игре
     * и его широковещательной рассылки всем участникам.
     *
     * Процесс состоит из трех шагов: сбор данных, формирование DTO сообщения, рассылка.
     *
     * @param gameCode Уникальный код текущей игры, для которой собирается состояние.
     * @param playerOrder Список идентификаторов игроков, определяющий порядок юнитов в сообщении.
     */
    public void broadcastUnitsState(String gameCode, List<String> playerOrder) {
        // 1. Сбор данных
        List<UnitStateDTO> units = collectUnitStates(gameCode, playerOrder);

        // 2. Формирование сообщения
        UnitsStateMessageDTO payload = createUnitsStateMessage(units);

        // 3. Рассылка
        broadcastMessage(gameCode, payload);
    }

// --- Вспомогательные методы ---

    /**
     * 1. Собирает состояния юнитов для указанной игры и порядка игроков.
     * @param gameCode Код игры.
     * @param playerOrder Список игроков в нужном порядке.
     * @return Список DTO состояний юнитов.
     */
    private List<UnitStateDTO> collectUnitStates(String gameCode, List<String> playerOrder) {
        List<UnitStateDTO> units = new ArrayList<>();

        for (String player : playerOrder) {
            PlayerUnit unit = unitRegistry.getUnit(gameCode, player);
            if (unit == null) continue;

            units.add(new UnitStateDTO(
                    unit.getId(),
                    player,
                    unit.getName(),
                    unit.getHealth(),
                    unit.getMaxHealth(),
                    unit.getImagePath(),
                    unit.getDeflectionCharges().current()
            ));
        }
        return units;
    }

    /**
     * 2. Создает DTO сообщения о состоянии юнитов.
     * @param units Список DTO состояний юнитов.
     * @return Объект сообщения.
     */
    private UnitsStateMessageDTO createUnitsStateMessage(List<UnitStateDTO> units) {
        return new UnitsStateMessageDTO(units);
    }

    /**
     * 3. Отправляет сообщение о состоянии юнитов всем активным сессиям в игре.
     * @param gameCode Код игры.
     * @param payload Объект сообщения для отправки.
     */
    private void broadcastMessage(String gameCode, Object payload) {
        Set<WebSocketSession> sessions = sessionRegistry.getSessions(gameCode);

        for (WebSocketSession session : sessions) {
            messageDispatcher.send(session, payload);
        }
    }

}
