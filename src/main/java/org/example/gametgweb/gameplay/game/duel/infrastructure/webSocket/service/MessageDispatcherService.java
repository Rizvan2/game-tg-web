package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.MessageFormatter;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.RoomSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class MessageDispatcherService {

    private final RoomSessionRegistry registry;
    private final MessageFormatter formatter;

    @Autowired
    public MessageDispatcherService(RoomSessionRegistry registry, MessageFormatter formatter) {
        this.registry = registry;
        this.formatter = formatter;
    }

    public void broadcastChat(String gameCode, String playerName, String text) {
        String msg = formatter.chatMessage(playerName, text);
        registry.broadcast(gameCode, msg);
    }

    public void broadcastJoin(String gameCode, String playerName) {
        registry.broadcast(gameCode, formatter.joinMessage(playerName, gameCode));
    }

    public void broadcastLeave(String gameCode, String playerName) {
        registry.broadcast(gameCode, formatter.leaveMessage(playerName, gameCode));
    }

    public void sendToPlayer(String gameCode, String playerName, String message) {
        registry.sendToPlayer(gameCode, playerName, message);
    }
    public void send(WebSocketSession session, Object payload) {
        String message = formatter.format(payload);
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
