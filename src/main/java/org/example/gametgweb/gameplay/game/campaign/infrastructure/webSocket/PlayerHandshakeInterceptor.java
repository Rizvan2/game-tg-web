package org.example.gametgweb.gameplay.game.campaign.infrastructure.webSocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Интерцептор WebSocket handshake для привязки игрока к сессии.
 * <p>
 * Достаёт имя игрока из {@link java.security.Principal} (Spring Security или другого источника)
 * и сохраняет его в атрибутах сессии под ключом "PLAYER_NAME".
 * <p>
 * Это позволяет WebSocketHandler'ам получать имя игрока после подключения.
 */
@Component
public class PlayerHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * Вызывается перед WebSocket handshake.
     * Извлекает Principal из запроса и добавляет имя игрока в атрибуты.
     *
     * @param request  HTTP-запрос
     * @param response HTTP-ответ
     * @param wsHandler обработчик WebSocket
     * @param attributes карта атрибутов сессии
     * @return true для продолжения handshake
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var principal = ((ServletServerHttpRequest) request).getServletRequest().getUserPrincipal();
        if (principal != null) {
            attributes.put("PLAYER_NAME", principal.getName());
        }
        return true;
    }

    /**
     * Вызывается после WebSocket handshake.
     * В данном классе реализация пустая.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param wsHandler обработчик WebSocket
     * @param exception исключение, если handshake завершился ошибкой
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}
