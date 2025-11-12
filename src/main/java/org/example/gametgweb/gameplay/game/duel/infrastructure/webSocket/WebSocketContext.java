package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import org.springframework.web.socket.WebSocketSession;

/**
 * Контекст WebSocket-сессии, содержащий ключевые данные:
 * <ul>
 *     <li>gameCode — уникальный код игровой комнаты;</li>
 *     <li>playerName — имя игрока (если известно).</li>
 * </ul>
 * <p>
 * Используется для централизованного доступа к атрибутам сессии
 * и предотвращения дублирования кода в обработчиках WebSocket.
 */
public record WebSocketContext(String gameCode, String playerName) {

    /**
     * Создает {@link WebSocketContext} из WebSocket-сессии.
     * <p>
     * Извлекает код комнаты из query-параметров URL
     * и имя игрока через {@link PrincipalUtils}.
     * <p>
     * Сохраняет значения в атрибутах сессии:
     * <ul>
     *     <li>"GAME_CODE" — код комнаты;</li>
     *     <li>"PLAYER_NAME" — имя игрока (если доступно).</li>
     * </ul>
     *
     * @param session WebSocket-сессия
     * @return объект {@link WebSocketContext}, либо {@code null}, если gameCode отсутствует
     */
    public static WebSocketContext from(WebSocketSession session) {
        if (session == null || session.getUri() == null) return null;

        // Извлекаем query-параметр gameCode
        String query = session.getUri().getQuery();
        String gameCode = WebSocketUtils.extractQueryParam(query, "gameCode");
        if (gameCode == null || gameCode.isBlank()) return null;

        // Получаем имя игрока через PrincipalUtils
        String playerName = PrincipalUtils.resolvePlayerName(session.getPrincipal());

        // Сохраняем данные в атрибутах сессии
        session.getAttributes().put("GAME_CODE", gameCode);
        if (playerName != null) {
            session.getAttributes().put("PLAYER_NAME", playerName);
        }

        return new WebSocketContext(gameCode, playerName);
    }
}
