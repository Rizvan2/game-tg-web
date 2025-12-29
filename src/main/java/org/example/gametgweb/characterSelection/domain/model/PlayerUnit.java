package org.example.gametgweb.characterSelection.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.GameUnit;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

/**
 * Хранит индивидуальное состояние юнита игрока
 */
@Getter
@Setter
@Slf4j
public class PlayerUnit implements GameUnit {

    private Long id;

    /**
     * Шаблон юнита (не меняется никогда)
     */
    private final Unit template;

    /**
     * Кастомные свойства игрока
     */
    private long health;
    private long maxHealth;

    /**
     * Кастомные визуальные элементы
     */
    private String name;
    private String imagePath;
    private long damage;

    public PlayerUnit(Unit template, String customName) {
        this.template = template;

        // Начальные параметры берём из шаблона
        this.name = customName;
        this.health = template.getHealth();
        this.maxHealth = template.getMaxHealth();
        this.imagePath = template.getImagePath();
        this.damage = template.getDamage();
    }

    public PlayerUnit(long id, Unit template, String name, long maxHealth, long health, long damage, String imagePath) {
        this.id = id;
        this.template = template;
        this.health = health;
        this.maxHealth = maxHealth;
        this.name = name;
        this.imagePath = imagePath;
        this.damage = damage;
    }

    /**
     * Сброс состоянию юнита до базового шаблонного
     */
    public void resetToTemplate() {
        this.health = template.getHealth();
        this.maxHealth = template.getMaxHealth();
        this.imagePath = template.getImagePath();
        this.damage = template.getDamage();
    }

    /**
     * Наносит урон юниту с учётом части тела, в которую пришёл удар.
     * <p>
     * Модификатор урона определяется значением {@link Body#getDamageMultiplier()}.
     * Если итоговый урон превышает текущее здоровье — здоровье устанавливается в 0.
     *
     * @param bodyPart часть тела, в которую попал удар
     * @param damage   базовое значение урона (до применения множителя)
     */
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

    /**
     * Вылечить юнита.
     * <p>
     * Если лечение превышает максимальное здоровье, здоровье устанавливается в {@link #maxHealth}.
     *
     * @param amount количество восстанавливаемого здоровья
     */
    @Override
    public void heal(long amount) {
        if (amount < 0) throw new IllegalArgumentException("Heal amount cannot be negative");
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    /**
     * Проверка жив ли юнит
     */
    public boolean isAlive() {
        return health > 0;
    }
}
