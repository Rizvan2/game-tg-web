package org.example.gametgweb.gameplay.game.duel.application.events;

import lombok.Getter;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.context.ApplicationEvent;

/**
 * Событие доменного уровня, сигнализирующее о полном уничтожении части тела персонажа
 * в рамках дуэли.
 *
 * <p>Используется для реакции подсистем (логирование, анимации, UI, расчёт эффектов,
 * сохранение статистики и т.д.) без прямой связности с игровой логикой.</p>
 *
 * <p>Событие содержит контекст игры, игрока и конкретную часть тела,
 * которая была уничтожена.</p>
 */
@Getter
public class BodyPartDestroyedEvent extends ApplicationEvent {

    /**
     * Уникальный код текущей игровой сессии (дуэли).
     */
    private final String gameCode;

    /**
     * Имя игрока, у которого была уничтожена часть тела.
     */
    private final String playerName;

    /**
     * Часть тела, которая была уничтожена в результате игрового действия.
     */
    private final Body bodyPart;

    /**
     * Создаёт событие уничтожения части тела.
     *
     * @param source объект-источник события (обычно сервис или агрегат)
     * @param gameCode уникальный идентификатор игры
     * @param playerName имя игрока
     * @param bodyPart уничтоженная часть тела
     */
    public BodyPartDestroyedEvent(
            Object source,
            String gameCode,
            String playerName,
            Body bodyPart
    ) {
        super(source);
        this.gameCode = gameCode;
        this.playerName = playerName;
        this.bodyPart = bodyPart;
    }
}
