package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сервис управления порядком игроков в конкретной игровой комнате.
 * <p>
 * Используется для:
 * <ul>
 *     <li>Фиксирования первых подключившихся игроков (определение ― кто "левый", кто "правый")</li>
 *     <li>Поддержки порядка при переподключениях</li>
 *     <li>Сохранения устойчивой позиции игрока в UI</li>
 * </ul>
 * <p>
 * Потокобезопасен благодаря {@link ConcurrentHashMap}, однако операции с {@link List}
 * требуют аккуратности — используется модель «один поток на комнату», что в обычных WebSocket-играх допустимо.
 */
@Component
@Slf4j
public class PlayerOrderService {

    /**
     * Хранит порядок подключений игроков.
     * <p>
     * Ключ — gameCode комнаты.
     * Значение — список имён игроков в порядке подключения.
     */
    private final Map<String, List<String>> orderMap = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> offlinePlayers = new ConcurrentHashMap<>();
    private final ExecutorService scheduler =
            Executors.newVirtualThreadPerTaskExecutor();
    /**
     * Добавляет игрока в порядок, если его ещё нет в списке.
     * <p>
     * Используется при первичном подключении и при переподключении
     * (если игрок отсутствует — значит, он новый).
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     */
    public void addPlayer(String gameCode, String playerName) {
        orderMap.computeIfAbsent(gameCode, k -> new ArrayList<>());
        List<String> order = orderMap.get(gameCode);

        if (!order.contains(playerName)) {
            order.add(playerName);
            log.info("Добавлен в порядок игрок {} в комнате {}", playerName, gameCode);
        }
    }

    public void removePlayer(String gameCode, String playerName) {
        List<String> order = orderMap.get(gameCode);
        if (order != null && order.contains(playerName)) {
            markOffline(gameCode, playerName);
            scheduleRemoval(gameCode, playerName, order);
        }
    }

    private void markOffline(String gameCode, String playerName) {
        offlinePlayers.computeIfAbsent(gameCode, k -> new HashSet<>()).add(playerName);
        log.info("Игрок {} помечен offline в комнате {}", playerName, gameCode);
    }

    private void scheduleRemoval(String gameCode, String playerName, List<String> order) {
        scheduler.submit(() -> {
            try {
                Thread.sleep(Duration.ofSeconds(30).toMillis());
                finalizeRemovalIfStillOffline(gameCode, playerName, order);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void finalizeRemovalIfStillOffline(String gameCode, String playerName, List<String> order) {
        if (isOffline(gameCode, playerName)) {
            order.remove(playerName);
            offlinePlayers.getOrDefault(gameCode, Set.of()).remove(playerName);

            if (order.isEmpty()) {
                orderMap.remove(gameCode);
            }

            log.info("Игрок {} удалён из комнаты {} после 30 секунд offline",
                    playerName, gameCode);
        }
    }

    /**
     * Проверяет, присутствует ли игрок в порядке комнаты.
     * <p>
     * Полезно при переподключении: если игрок уже был — позиция сохраняется.
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     * @return true — если игрок уже есть в списке
     */
    public boolean contains(String gameCode, String playerName) {
        return orderMap.getOrDefault(gameCode, List.of()).contains(playerName);
    }

    public boolean isOffline(String gameCode, String playerName) {
        return offlinePlayers.getOrDefault(gameCode, Set.of()).contains(playerName);
    }

    public void markOnline(String gameCode, String playerName) {
        Set<String> offline = offlinePlayers.get(gameCode);
        if (offline != null) offline.remove(playerName);
    }

    /**
     * Возвращает текущий порядок игроков (неизменяемый список).
     *
     * @param gameCode код игровой комнаты
     * @return порядок игроков или пустой список, если комнаты нет
     */
    public List<String> getOrder(String gameCode) {
        return orderMap.getOrDefault(gameCode, List.of());
    }
}
