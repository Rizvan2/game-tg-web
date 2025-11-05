package org.example.gametgweb.gameplay.Campaign.webSocket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.Campaign.entity.Campaign;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CampaignSessionRegistry {

    private final Map<String, Set<WebSocketSession>> sessionsPerPlayer = new ConcurrentHashMap<>();
    private final Map<String, Campaign> campaigns = new ConcurrentHashMap<>();

    public void addSession(String playerName, WebSocketSession session) {
        sessionsPerPlayer
                .computeIfAbsent(playerName, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        log.info("Добавлена сессия {} игроку {}", session.getId(), playerName);
    }

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

    public void putCampaign(String playerName, Campaign campaign) {
        campaigns.put(playerName, campaign);
    }

    public Campaign getCampaign(String playerName) {
        return campaigns.get(playerName);
    }

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
