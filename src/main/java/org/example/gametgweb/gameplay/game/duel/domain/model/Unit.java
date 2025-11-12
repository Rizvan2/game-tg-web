package org.example.gametgweb.gameplay.game.duel.domain.model;

import lombok.Getter;

/**
 * Доменная модель юнита — отражает игровую логику, а не структуру таблицы.
 */
@Getter
public class Unit {

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
    public void takeDamage(long damage) {
        if (damage < 0) throw new IllegalArgumentException("Damage cannot be negative");
        this.health = Math.max(0, this.health - damage);
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
