package org.example.gametgweb.characterSelection.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.valueObjects.DeflectionCharges;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.BodyPartEfficiency;
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
    private long damage;
    private BodyPartEfficiency bodyEfficiency;
    private DeflectionCharges deflectionCharges;

    /**
     * Кастомные визуальные элементы
     */
    private String name;
    private String imagePath;

    public PlayerUnit(Unit template, String customName) {
        this.template = template;

        // Начальные параметры берём из шаблона
        this.name = customName;
        this.health = template.getHealth();
        this.maxHealth = template.getMaxHealth();
        this.imagePath = template.getImagePath();
        this.damage = template.getDamage();
        // ✅ ИСПРАВЬ ЭТО: создаём КОПИЮ, а не ссылку
        if (template.getBodyEfficiency() != null) {
            this.bodyEfficiency = new BodyPartEfficiency(template.getBodyEfficiency());
        } else {
            // Fallback на случай если в шаблоне нет
            this.bodyEfficiency = new BodyPartEfficiency(
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0,
                    1.0);
        }
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
     * @return текущая эффективность атакованной части тела после урона (0.0 - 1.0+)
     */
    @Override
    public double takeDamage(Body bodyPart, long damage) {
        // Рассчитываем фактический урон с модификатором
        long actualDamage = Math.round(damage * bodyPart.getDamageMultiplier());

        // Применяем урон
        this.health = Math.max(this.health - actualDamage, 0);

        log.info("{} получает {} урона в {} (x{})",
                name, actualDamage, bodyPart.name(), bodyPart.getDamageMultiplier());

        // Возвращаем эффективность атакованной части тела
        return bodyEfficiency.reduceEfficiency(bodyPart, actualDamage, maxHealth);
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
