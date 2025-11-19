package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

/**
 * Интерфейс для игровых юнитов.
 * <p>
 * Определяет базовые методы для работы с состоянием юнита:
 * имя, здоровье, максимальное здоровье, урон, получение урона и лечение.
 */
public interface GameUnit {

    /**
     * Получить имя юнита.
     *
     * @return имя юнита
     */
    String getName();

    /**
     * Получить текущее здоровье юнита.
     *
     * @return текущее здоровье
     */
    long getHealth();

    /**
     * Получить максимальное здоровье юнита.
     *
     * @return максимальное здоровье
     */
    long getMaxHealth();

    /**
     * Получить значение урона юнита.
     *
     * @return урон
     */
    long getDamage();

    /**
     * Нанесение урона юниту по указанной части тела.
     *
     * @param bodyPart часть тела, по которой наносится урон
     * @param damage количество урона
     */
    void takeDamage(Body bodyPart, long damage);

    /**
     * Восстановление здоровья юнита.
     *
     * @param amount количество восстанавливаемого здоровья
     */
    void heal(long amount);
}
