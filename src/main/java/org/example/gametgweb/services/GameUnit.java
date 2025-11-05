package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.Body;

public interface GameUnit {

    String getName();
    long getHealth();
    long getMaxHealth();
    long getDamage();

    // Нанесение урона
    void takeDamage(Body bodyPart, long damage);

    // Лечение юнита
    void heal(long amount);
}
