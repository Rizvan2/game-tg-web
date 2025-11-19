package org.example.gametgweb.characterSelection.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.GameUnit;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

/**
 * Доменная модель юнита — отражает игровую логику, а не структуру таблицы.
 */
@Getter
@Slf4j
public class Unit implements GameUnit {

    private final long id;
    private final String name;
    private final long maxHealth;
    private long health;
    private final long damage;
    private final String imagePath;

    public Unit(long id, String name, long maxHealth, long health, long damage, String imagePath) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = health;
        this.damage = damage;
        this.imagePath = imagePath;
    }

    /** Нанесение урона */
    @Override
    public void takeDamage(Body bodyPart, long damage) {
        if (bodyPart == null) {
            throw new IllegalArgumentException("Часть тела не может быть null");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("Урон не может быть отрицательным");
        }

        // Рассчитываем фактический урон с модификатором
        long actualDamage = Math.round(damage * bodyPart.getDamageMultiplier());

        // Применяем урон
        this.health = Math.max(this.health - actualDamage, 0);

        log.info("{} получает {} урона в {} (x{})",
                name, actualDamage, bodyPart.name(), bodyPart.getDamageMultiplier());
    }

    /** Лечение */
    public void heal(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Heal amount cannot be negative");
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    /** Проверка жив ли юнит */
    public boolean isAlive() {
        return health > 0;
    }
}
