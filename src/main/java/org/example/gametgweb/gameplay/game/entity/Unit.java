package org.example.gametgweb.gameplay.game.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.Body;
import org.example.gametgweb.services.GameUnit;

/**
 * Сущность игрового юнита.
 * <p>
 * Содержит основные характеристики юнита:
 * имя, здоровье, максимальное здоровье, урон и путь к изображению.
 * Также включает методы для нанесения урона и лечения.
 */
@Entity
@Table(name = "units") // принято писать имена таблиц в нижнем регистре
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class Unit implements GameUnit {

    /** Уникальный идентификатор юнита. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /** Имя юнита, уникальное и обязательное. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Максимальное здоровье юнита. */
    @Column(nullable = false)
    private long maxHealth;

    /** Текущее здоровье юнита. */
    @Column(nullable = false)
    private long health;

    /** Урон, который может нанести юнит. */
    @Column(nullable = false)
    private long damage;

    /** Путь к изображению юнита в ресурсах. По умолчанию: "/images/player1.png". */
    @Column(name = "image_path")
    private String imagePath;

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
