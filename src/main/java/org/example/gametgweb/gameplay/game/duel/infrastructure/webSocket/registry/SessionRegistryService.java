package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

/**
 * SessionRegistryService — сервис управления WebSocket-сессиями на уровне приложения.
 *
 * <p>Служит прослойкой между бизнес-логикой и низкоуровневым хранилищем сессий
 * ({@link RoomSessionRegistry}), добавляя логирование, управление атрибутами сессии
 * (например, именем игрока) и высокоуровневые операции (регистрация новой сессии, замена сессии).</p>
 */
@Service
@Slf4j
public class SessionRegistryService {

    private final RoomSessionRegistry registry;

    /**
     * Конструктор для внедрения зависимости хранилища сессий.
     *
     * @param registry Внутренний механизм хранения и управления сессиями по коду комнаты.
     */
    public SessionRegistryService(RoomSessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Добавляет новую WebSocket-сессию в указанную комнату.
     *
     * @param gameCode Код комнаты, к которой присоединяется сессия.
     * @param session  WebSocket-сессия, которую нужно зарегистрировать.
     */
    public void addSession(String gameCode, WebSocketSession session) {
        registry.addSession(gameCode, session);
        log.info("Сессия добавлена для комнаты {}", gameCode);
    }

    /**
     * Удаляет WebSocket-сессию из указанной комнаты.
     *
     * @param gameCode Код комнаты, из которой удаляется сессия.
     * @param session  WebSocket-сессия, которую нужно удалить.
     */
    public void removeSession(String gameCode, WebSocketSession session) {
        registry.removeSession(gameCode, session);
        log.info("Сессия удалена из комнаты {}", gameCode);
    }

    /**
     * Возвращает копию набора активных сессий для комнаты.
     *
     * @param gameCode код комнаты
     * @return множество WebSocket-сессий; если комнаты нет, возвращает пустой набор
     */
    public Set<WebSocketSession> getSessions(String gameCode) {
        // возвращаем mutable копию
        return registry.getSessions(gameCode);
    }

    /**
     * Заменяет старую сессию игрока новой в указанной комнате.
     *
     * <p>Этот метод требует реализации в {@link RoomSessionRegistry} логики
     * поиска старой сессии по имени игрока и её замены.</p>
     *
     * @param gameCode   Код комнаты.
     * @param playerName Имя игрока, чья сессия заменяется.
     * @param newSession Новая WebSocket-сессия.
     */
    public void replaceSession(String gameCode, String playerName, WebSocketSession newSession) {
        registry.replaceSession(gameCode, playerName, newSession); // метод нужно добавить в RoomSessionRegistry
    }

    /**
     * Полностью обрабатывает замену сессии игрока при переподключении.
     *
     * <p>Сначала прикрепляет имя игрока к новой сессии, затем выполняет замену через реестр.</p>
     *
     * @param gameCode   Код комнаты.
     * @param playerName Имя игрока.
     * @param newSession Новая WebSocket-сессия, пришедшая при реконнекте.
     */
    public void replacePlayerSession(String gameCode, String playerName, WebSocketSession newSession) {
        attachPlayerName(newSession, playerName);
        replaceSession(gameCode, playerName, newSession);
    }

    /**
     * Прикрепляет имя игрока к атрибутам WebSocket-сессии.
     *
     * @param session    Сессия, к которой прикрепляется имя.
     * @param playerName Имя игрока, которое нужно сохранить в сессии.
     */
    public void attachPlayerName(WebSocketSession session, String playerName) {
        session.getAttributes().put("PLAYER_NAME", playerName);
    }

    /**
     * Извлекает имя игрока из атрибутов WebSocket-сессии.
     *
     * @param session Сессия, из которой извлекается имя.
     * @return Имя игрока в виде строки, или {@code null}, если атрибут не найден.
     */
    public String getPlayerName(WebSocketSession session) {
        Object value = session.getAttributes().get("PLAYER_NAME");
        return value != null ? value.toString() : null;
    }

    /**
     * Полностью регистрирует новую сессию игрока (первичное присоединение).
     *
     * <p>Прикрепляет имя игрока к сессии и добавляет сессию в реестр комнаты.</p>
     *
     * @param gameCode   Код комнаты.
     * @param playerName Имя присоединяющегося игрока.
     * @param session    Новая WebSocket-сессия.
     */
    public void registerNewSession(String gameCode, String playerName, WebSocketSession session) {
        attachPlayerName(session, playerName);
        addSession(gameCode, session);
    }
}
