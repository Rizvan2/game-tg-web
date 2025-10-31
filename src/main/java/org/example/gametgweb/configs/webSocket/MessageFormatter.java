package org.example.gametgweb.configs.webSocket;

import org.springframework.stereotype.Component;

/**
 * MessageFormatter — компонент для формирования текстовых сообщений о событиях
 * входа и выхода игроков в комнаты WebSocket.
 * <p>
 * Используется для централизованного форматирования сообщений, чтобы избежать дублирования строк в коде.
 */
@Component
public class MessageFormatter {

    /**
     * Формирует сообщение о подключении игрока к комнате.
     *
     * @param playerName имя игрока, может быть {@code null}
     * @param gameCode   код комнаты
     * @return текстовое сообщение вида "{playerName} подключился к комнате {gameCode}!"
     */
    public String joinMessage(String playerName, String gameCode) {
        String name = playerName != null ? playerName : "Игрок";
        return name + " подключился к комнате " + gameCode + "!";
    }

    /**
     * Формирует сообщение о выходе игрока из комнаты.
     *
     * @param playerName имя игрока, может быть {@code null}
     * @param gameCode   код комнаты
     * @return текстовое сообщение вида "{playerName} вышел из комнаты {gameCode}!"
     */
    public String leaveMessage(String playerName, String gameCode) {
        String name = playerName != null ? playerName : "Игрок";
        return name + " вышел из комнаты " + gameCode + "!";
    }
}


