package org.example.gametgweb.gameplay.game.duel.webSocket;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * JoinLeaveScheduler — планировщик отложенных событий для WebSocket игроков.
 * <p>
 * Управляет входами и выходами игроков с небольшой задержкой,
 * учитывая быстрые перезагрузки страниц и дублирующиеся соединения.
 */
@Slf4j
@Component
public class JoinLeaveScheduler {

    private static final long JOIN_DELAY_MS = 2000L;
    private static final long LEAVE_DELAY_MS = 2000L;
    private static final long RELOAD_GRACE_MS = 3000L;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /** Отложенные задачи "вышел" для каждого игрока в каждой комнате */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ScheduledFuture<?>>> pendingLeaveTasks = new ConcurrentHashMap<>();

    /** Время последнего выхода игрока для подавления быстрого повторного join */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> lastLeaveAt = new ConcurrentHashMap<>();

    /** Отменяет отложенный выход игрока, если он вернулся */
    public boolean cancelPendingLeave(String gameCode, String playerName) {
        if (playerName == null) return false;
        ScheduledFuture<?> task = getMap(pendingLeaveTasks, gameCode).remove(playerName);
        if (task != null) {
            task.cancel(false);
            log.debug("Отложенный leave отменён для игрока {} в комнате {}", playerName, gameCode);
            return true;
        }
        return false;
    }

    /** Проверяет, нужно ли подавить сообщение о join после быстрого выхода */
    public boolean shouldSuppressJoin(String gameCode, String playerName) {
        if (playerName == null) return false;
        Long lastLeave = getMap(lastLeaveAt, gameCode).get(playerName);
        return lastLeave != null && (System.currentTimeMillis() - lastLeave) < RELOAD_GRACE_MS;
    }

    /** Планирует join-сообщение с задержкой */
    public void scheduleJoin(String gameCode, String playerName, Runnable action) {
        scheduler.schedule(action, JOIN_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /** Планирует leave-сообщение с задержкой */
    public void scheduleLeave(String gameCode, String playerName, Runnable action) {
        if (playerName == null) return;

        getMap(lastLeaveAt, gameCode).put(playerName, System.currentTimeMillis());

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            action.run();
            getMap(pendingLeaveTasks, gameCode).remove(playerName);
        }, LEAVE_DELAY_MS, TimeUnit.MILLISECONDS);

        getMap(pendingLeaveTasks, gameCode).put(playerName, future);
    }

    /** Обрабатывает join игрока с проверкой на быстрый повторный вход */
    public boolean handleJoin(WebSocketContext ctx, Runnable onJoin) {
        String gameCode = ctx.gameCode();
        String player = ctx.playerName();
        if (player == null) return false;

        cancelPendingLeave(gameCode, player);

        if (shouldSuppressJoin(gameCode, player)) {
            log.debug("[Join suppressed] Игрок {} вернулся в комнату {}", player, gameCode);
            return false;
        }

        scheduleJoin(gameCode, player, onJoin);
        return true;
    }

    /** Обрабатывает leave игрока */
    public void handleLeave(WebSocketContext ctx, Runnable onLeave) {
        String gameCode = ctx.gameCode();
        String player = ctx.playerName();
        if (player != null) {
            scheduleLeave(gameCode, player, onLeave);
        }
    }

    /** Завершает работу планировщика при остановке приложения */
    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        log.info("JoinLeaveScheduler завершил работу");
    }

    /** Вспомогательный метод: получает карту по ключу или создаёт новую */
    private <T> ConcurrentHashMap<String, T> getMap(ConcurrentHashMap<String, ConcurrentHashMap<String, T>> map, String key) {
        return map.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
    }
}