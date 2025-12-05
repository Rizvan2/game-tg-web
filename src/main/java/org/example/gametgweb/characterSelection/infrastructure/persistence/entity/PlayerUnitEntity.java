package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

/**
 * Хранит индивидуальное состояние юнита игрока
 */
@Getter
@Setter
@Slf4j
@Entity
public class PlayerUnitEntity implements GameUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Шаблон юнита (не меняется никогда)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private UnitEntity template;

    /**
     * Имя юнита, уникальное и обязательное.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Максимальное здоровье юнита.
     */
    @Column(nullable = false)
    private long maxHealth;

    /**
     * Текущее здоровье юнита.
     */
    @Column(nullable = false)
    private long health;

    /**
     * Урон, который может нанести юнит.
     */
    @Column(nullable = false)
    private long damage;

    /**
     * Путь к изображению юнита в ресурсах.
     */
    @Column(name = "image_path")
    private String imagePath;

    public PlayerUnitEntity(UnitEntity template) {
        this.template = template;

        // Начальные параметры берём из шаблона
        this.maxHealth = template.getMaxHealth();
        this.health = template.getHealth();
        this.damage = template.getDamage();
        this.name = template.getName();
        this.imagePath = template.getImagePath();
    }

    public PlayerUnitEntity(long id, UnitEntity template, String name, long maxHealth, long health, long damage, String imagePath) {
        this.id = id;
        this.template = template;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = health;
        this.damage = damage;
        this.imagePath = imagePath;
    }

    public PlayerUnitEntity() {
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

        // Рассчитываем фактический урон с модификатором
        long actualDamage = Math.round(damage * bodyPart.getDamageMultiplier());

        // Применяем урон
        this.health = Math.max(this.health - actualDamage, 0);

        // Можно добавить лог/сообщение
        log.info("%s получает %d урона в %s (x%.2f)%n",
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
        this.health = Math.min(this.health + amount, this.maxHealth);
    }
}
