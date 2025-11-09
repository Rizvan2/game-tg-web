package org.example.gametgweb.gameplay.game.campaign.webSocket;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Регистр сессий WebSocket для кампаний.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>Хранение активных WebSocket-сессий игроков;</li>
 *     <li>Связь игрока с текущей кампанией;</li>
 *     <li>Отправку сообщений всем активным сессиям игрока (broadcast).</li>
 * </ul>
 * <p>
 * Использует потоко-безопасные коллекции для работы с несколькими сессиями одновременно.
 */
@Slf4j
@Component
public class CampaignSessionRegistry {

    /** Сессии игроков: playerName → набор WebSocketSession */
    private final Map<String, Set<WebSocketSession>> sessionsPerPlayer = new ConcurrentHashMap<>();

    /** Активные кампании игроков: playerName → Campaign */
    private final Map<String, Campaign> campaigns = new ConcurrentHashMap<>();

    /**
     * Добавляет WebSocket-сессию игрока.
     *
     * @param playerName имя игрока
     * @param session    WebSocket-сессия
     */
    public void addSession(String playerName, WebSocketSession session) {
        sessionsPerPlayer
                .computeIfAbsent(playerName, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        log.info("Добавлена сессия {} игроку {}", session.getId(), playerName);
    }

    /**
     * Удаляет WebSocket-сессию игрока.
     * Если после удаления у игрока не остаётся сессий,
     * также удаляет данные кампании.
     *
     * @param playerName имя игрока
     * @param session    WebSocket-сессия
     */
    public void removeSession(String playerName, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsPerPlayer.get(playerName);
        if (sessions != null) {
            sessions.remove(session);
            log.info("Удалена сессия {} игрока {}", session.getId(), playerName);
            if (sessions.isEmpty()) {
                sessionsPerPlayer.remove(playerName);
                campaigns.remove(playerName);
                log.info("Игрок {} не имеет активных сессий — данные кампании удалены", playerName);
            }
        }
    }

    /**
     * Сохраняет кампанию для конкретного игрока.
     *
     * @param playerName имя игрока
     * @param campaign   объект кампании
     */
    public void putCampaign(String playerName, Campaign campaign) {
        campaigns.put(playerName, campaign);
    }

    /**
     * Получает текущую кампанию игрока.
     *
     * @param playerName имя игрока
     * @return объект Campaign или null, если нет
     */
    public Campaign getCampaign(String playerName) {
        return campaigns.get(playerName);
    }

    /**
     * Отправляет сообщение всем активным сессиям игрока.
     * Закрытые сессии автоматически удаляются из регистра.
     *
     * @param playerName имя игрока
     * @param message    текст сообщения
     */
    public void broadcast(String playerName, String message) {
        Set<WebSocketSession> sessions = sessionsPerPlayer.get(playerName);
        if (sessions == null || sessions.isEmpty()) return;

        sessions.removeIf(s -> !s.isOpen());
        sessions.forEach(s -> {
            try { s.sendMessage(new TextMessage(message)); }
            catch (IOException e) { log.error("Ошибка отправки сообщения: {}", e.getMessage()); }
        });
    }
}
