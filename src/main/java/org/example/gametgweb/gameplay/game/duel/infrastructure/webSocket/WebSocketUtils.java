package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WebSocketUtils {

    private WebSocketUtils() {}

    /**
     * Извлекает все query-параметры из строки в Map.
     *
     * @param query query-строка (например, "gameCode=abc123&mode=duel")
     * @return Map<ключ, значение>, пустая Map если query==null
     */
    public static Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isBlank()) return Collections.emptyMap();

        return Stream.of(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(arr -> arr.length == 2 && !arr[0].isBlank())
                .collect(Collectors.toMap(
                        arr -> URLDecoder.decode(arr[0], StandardCharsets.UTF_8),
                        arr -> URLDecoder.decode(arr[1], StandardCharsets.UTF_8)
                ));
    }

    /**
     * Получает значение конкретного параметра из query-строки.
     *
     * @param query query-строка
     * @param key   имя параметра
     * @return значение параметра или null если отсутствует
     */
    public static String extractQueryParam(String query, String key) {
        return parseQueryParams(query).get(key);
    }
}
