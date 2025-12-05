package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelTurn;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.DuelTurnManager;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.RoomSessionRegistry;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DuelCombatService — сервис обработки логики боя между двумя игроками в дуэли.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>Добавление хода игрока через {@link DuelTurnManager};</li>
 *     <li>Определение готовности хода (когда оба игрока сделали выбор);</li>
 *     <li>Вызов {@link CombatService#duelRound(PlayerUnit, Body, PlayerUnit, Body)} для расчёта результатов боя;</li>
 *     <li>Очистку хода после завершения раунда;</li>
 *     <li>Возврат результата боя в виде JSON строки.</li>
 * </ul>
 */
@Component
public class DuelCombatService {

    private final DuelTurnManager turnManager;
    private final CombatService combatService;
    private final RoomSessionRegistry roomSessionRegistry;
    private final UnitRegistryService unitRegistryService;
    private final ObjectMapper objectMapper;
    /**
     * Потокобезопасная карта для хранения объектов-мониторов, используемых для синхронизации
     * доступа к ходу дуэли (DuelTurn) по коду комнаты.
     * Key: gameCode, Value: Object (монитор блокировки)
     */
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param turnManager         Сервис управления текущим ходом дуэли.
     * @param combatService       Сервис бизнес-логики, выполняющий расчёт одного раунда боя.
     * @param roomSessionRegistry Реестр сессий для широковещательной рассылки уведомлений.
     * @param unitRegistryService Реестр юнитов для получения текущего состояния юнитов перед расчетом.
     * @param objectMapper        Объект для сериализации ответов в JSON.
     */
    @Autowired
    public DuelCombatService(DuelTurnManager turnManager, CombatService combatService, RoomSessionRegistry roomSessionRegistry, UnitRegistryService unitRegistryService, ObjectMapper objectMapper) {
        this.turnManager = turnManager;
        this.combatService = combatService;
        this.roomSessionRegistry = roomSessionRegistry;
        this.unitRegistryService = unitRegistryService;
        this.objectMapper = objectMapper;
    }

    /**
     * Обрабатывает входящий запрос на атаку от игрока.
     *
     * <p>Вся логика выполнения хода, проверки готовности и расчета раунда
     * защищена блоком {@code synchronized (roomLock)} для обеспечения потокобезопасности
     * при одновременных ходах от двух игроков.</p>
     *
     * @param gameCode Код комнаты дуэли.
     * @param player   Имя игрока, совершившего ход.
     * @param body     Выбранное игроком тело для атаки (Body).
     * @return JSON-строка с результатом раунда, если оба игрока сделали ход; {@code null}, если ожидается второй игрок.
     * @throws Exception Ошибка при выполнении операций.
     */
    public String processAttack(String gameCode, String player, Body body) throws Exception {
        Object roomLock = locks.computeIfAbsent(gameCode, k -> new Object());

        synchronized (roomLock) {
            DuelTurn turn = turnManager.addMove(gameCode, player, body);

            // игрок нажал "Атаковать"
            turn.setReady(player);

            // уведомление о том, что оба игрока сделали выбор
            selectionNotification(turn, gameCode);

            // если оба игрока нажали "Атаковать" → считаем раунд
            return readingRound(turn, gameCode);
        }
    }

    /**
     * Отправляет широковещательное уведомление всем игрокам в комнате, что оба игрока
     * сделали свой выбор хода, если это еще не было сделано.
     *
     * <p>Эта операция выполняется внутри синхронизированного блока, что гарантирует,
     * что уведомление будет отправлено строго один раз.</p>
     *
     * @param turn     Текущий объект хода дуэли.
     * @param gameCode Код комнаты.
     * @throws JsonProcessingException если произошла ошибка при сериализации JSON-сообщения.
     */
    private void selectionNotification(DuelTurn turn, String gameCode) throws JsonProcessingException {
        // уведомление о том, что оба игрока сделали выбор
        if (turn.isReady() && !turn.isBothSelectedNotified()) {
            turn.setBothSelectedNotified(true);
            roomSessionRegistry.broadcast(
                    gameCode,
                    objectMapper.writeValueAsString(Map.of("type", "bothSelected"))
            );
        }
    }

    /**
     * Выполняет расчет раунда дуэли, если оба игрока готовы.
     *
     * <p>Этот метод вызывается внутри синхронизированного блока, гарантируя, что
     * только один поток выполнит расчет, обновит юниты и удалит ход.</p>
     *
     * @param turn     Текущий объект хода дуэли.
     * @param gameCode Код игровой комнаты.
     * @return JSON-строка с результатом раунда, если оба готовы; {@code null}, если ожидается второй игрок.
     * @throws JsonProcessingException если произошла ошибка при сериализации JSON.
     */
    private String readingRound(DuelTurn turn, String gameCode) throws JsonProcessingException {
        if (turn.isReady()) {
            PlayerUnit u1 = unitRegistryService.getUnit(gameCode, turn.getPlayer1());
            PlayerUnit u2 = unitRegistryService.getUnit(gameCode, turn.getPlayer2());

            Map<String, Object> result = combatService.duelRound(u1, turn.getBody1(), u2, turn.getBody2());

            // Добавляем явные поля для фронта
            Map<String, Object> response = new HashMap<>(result);
            response.put("attacker", u1.getName());
            response.put("defender", u2.getName());

            // очищаем ход после раунда
            turnManager.removeTurn(gameCode);

            return objectMapper.writeValueAsString(response);
        }

        return null; // ждём второго игрока
    }
}
