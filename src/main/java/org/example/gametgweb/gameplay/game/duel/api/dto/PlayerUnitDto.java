package org.example.gametgweb.gameplay.game.duel.api.dto;

/**
 * DTO (Data Transfer Object) для передачи информации об активном юните игрока
 * между сервером и клиентом (например, фронтендом лобби).
 * <p>
 * Используется для сериализации в JSON и содержит только необходимые поля для отображения:
 * <ul>
 *     <li>{@code name} — имя юнита</li>
 *     <li>{@code imagePath} — путь к изображению юнита</li>
 *     <li>{@code health} — текущее здоровье юнита</li>
 *     <li>{@code maxHealth} — максимальное здоровье юнита</li>
 *     <li>{@code damage} — урон юнита</li>
 * </ul>
 *
 * <p>Пример JSON, который вернёт контроллер:</p>
 * <pre>
 * {
 *   "name": "Воин",
 *   "imagePath": "/images/warrior.png",
 *   "health": 80,
 *   "maxHealth": 100,
 *   "damage": 15
 * }
 * </pre>
 */
public record PlayerUnitDto(
        String name,
        String imagePath,
        long health,
        long maxHealth,
        long damage
) { }
