package org.example.gametgweb.gameplay.game.Duel;

import org.example.gametgweb.gameplay.game.GameState;

/**
 * Доменный класс, представляющий игровой матч в Telegram-боте.
 * <p>
 * Этот класс является {@link java.lang.Record}, что делает его immutable по умолчанию.
 * Содержит информацию о:
 * <ul>
 *     <li>Уникальном идентификаторе матча</li>
 *     <li>Коде игры для подключения игроков</li>
 *     <li>Текущем состоянии игры</li>
 *     <li>Списке игроков, участвующих в матче</li>
 *     <li>Времени создания и последнего обновления матча</li>
 * </ul>
 * <p>
 * Immutable поля позволяют безопасно передавать объект по системе без риска случайного изменения состояния.
 */
public record Game(
        Long id,
        String gameCode,
        GameState state
) {}
