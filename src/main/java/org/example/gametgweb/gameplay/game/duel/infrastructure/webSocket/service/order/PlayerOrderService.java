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
 *
 * <p>Используется для:
 * <ul>
 * <li>Фиксирования первых подключившихся игроков (определение ― кто "левый", кто "правый")</li>
 * <li>Поддержки порядка при переподключениях</li>
 * <li>Сохранения устойчивой позиции игрока в UI</li>
 * <li>Обработки временного отключения (offline) и удаления игрока после таймаута.</li>
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

    /**
     * Хранит имена игроков, которые временно отключились (оффлайн).
     * <p>
     * Ключ — gameCode комнаты.
     * Значение — множество имён игроков, помеченных как оффлайн.
     */
    private final Map<String, Set<String>> offlinePlayers = new ConcurrentHashMap<>();

    /**
     * Пул потоков для планирования задач отложенного удаления (таймаута).
     * Использует виртуальные потоки для эффективного управления большим количеством задач.
     */
    private final ExecutorService scheduler =
            Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Добавляет игрока в порядок, если его ещё нет в списке.
     *
     * <p>Используется при первичном подключении и при переподключении.
     * Если игрок уже существует, его позиция в списке не меняется, что гарантирует стабильность порядка.</p>
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

    /**
     * Обрабатывает отключение игрока, помечая его как оффлайн и запуская таймер удаления.
     *
     * <p>Игрок не удаляется сразу, что дает ему время на переподключение (реконнект).</p>
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока, который отключился
     */
    public void removePlayer(String gameCode, String playerName) {
        List<String> order = orderMap.get(gameCode);
        if (order != null && order.contains(playerName)) {
            markOffline(gameCode, playerName);
            scheduleRemoval(gameCode, playerName, order);
        }
    }

    /**
     * Помечает игрока как оффлайн в соответствующем хранилище.
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     */
    private void markOffline(String gameCode, String playerName) {
        offlinePlayers.computeIfAbsent(gameCode, k -> new HashSet<>()).add(playerName);
        log.info("Игрок {} помечен offline в комнате {}", playerName, gameCode);
    }

    /**
     * Планирует удаление игрока через 30 секунд.
     *
     * <p>Если за это время игрок переподключится, задача удаления будет проигнорирована
     * при финальной проверке.</p>
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     * @param order      ссылка на список порядка (для упрощения доступа)
     */
    private void scheduleRemoval(String gameCode, String playerName, List<String> order) {
        scheduler.submit(() -> {
            try {
                Thread.sleep(Duration.ofSeconds(30).toMillis());
                finalizeRemovalIfStillOffline(gameCode, playerName, order);
            } catch (InterruptedException e) {
                log.warn("Планировщик удаления был прерван для игрока {} в комнате {}", playerName, gameCode);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Выполняет окончательное удаление игрока, если он все еще помечен как оффлайн.
     *
     * <p>Если список игроков после удаления становится пустым, комната также удаляется из {@code orderMap}.</p>
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     * @param order      список порядка игроков для текущей комнаты
     */
    private void finalizeRemovalIfStillOffline(String gameCode, String playerName, List<String> order) {
        if (isOffline(gameCode, playerName)) {
            order.remove(playerName);

            // Удаляем игрока из набора оффлайн
            offlinePlayers.getOrDefault(gameCode, Set.of()).remove(playerName);

            if (order.isEmpty()) {
                orderMap.remove(gameCode);
                offlinePlayers.remove(gameCode); // Очищаем и оффлайн-карту для комнаты
                log.info("Комната {} удалена, так как все игроки вышли.", gameCode);
            }

            log.info("Игрок {} удалён из комнаты {} после 30 секунд offline",
                    playerName, gameCode);
        }
    }

    /**
     * Проверяет, присутствует ли игрок в порядке комнаты.
     *
     * <p>Полезно при переподключении: если игрок уже был — его позиция сохраняется.</p>
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     * @return {@code true} — если игрок уже есть в списке, иначе {@code false}.
     */
    public boolean contains(String gameCode, String playerName) {
        return orderMap.getOrDefault(gameCode, List.of()).contains(playerName);
    }

    /**
     * Проверяет, помечен ли игрок как временно отключенный (оффлайн).
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     * @return {@code true} — если игрок находится в наборе оффлайн, иначе {@code false}.
     */
    public boolean isOffline(String gameCode, String playerName) {
        return offlinePlayers.getOrDefault(gameCode, Set.of()).contains(playerName);
    }

    /**
     * Помечает игрока как онлайн, удаляя его из набора оффлайн игроков.
     *
     * <p>Используется при успешном переподключении, отменяя запланированное удаление.</p>
     *
     * @param gameCode   код игровой комнаты
     * @param playerName имя игрока
     */
    public void markOnline(String gameCode, String playerName) {
        Set<String> offline = offlinePlayers.get(gameCode);
        if (offline != null) {
            boolean wasOffline = offline.remove(playerName);
            if (wasOffline) {
                log.info("Игрок {} успешно переподключился (markOnline) к комнате {}", playerName, gameCode);
            }
        }
    }

    /**
     * Возвращает текущий порядок игроков (неизменяемый список).
     *
     * @param gameCode код игровой комнаты
     * @return неизменяемый список имён игроков в порядке подключения или пустой список, если комнаты нет.
     */
    public List<String> getOrder(String gameCode) {
        // Возвращаем неизменяемую копию для предотвращения внешних модификаций
        List<String> order = orderMap.get(gameCode);
        return order != null ? Collections.unmodifiableList(order) : List.of();
    }
}