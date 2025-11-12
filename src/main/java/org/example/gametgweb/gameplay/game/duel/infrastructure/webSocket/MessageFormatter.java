package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

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

    /**
     * Формирует сообщение чата для отправки всем игрокам комнаты.
     *
     * @param playerName имя игрока, отправившего сообщение
     * @param message    текст сообщения
     * @return текстовое сообщение вида "[playerName]: message"
     */
    public String chatMessage(String playerName, String message) {
        String name = playerName != null ? playerName : "Игрок";
        String text = message != null ? message : "";
        return "[" + name + "]: " + text;
    }
}


