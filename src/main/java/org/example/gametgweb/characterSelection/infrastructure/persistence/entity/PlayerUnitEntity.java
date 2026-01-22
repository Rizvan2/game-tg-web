package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Хранит индивидуальное состояние юнита игрока
 */
@Getter
@Setter
@Slf4j
@Entity
public class PlayerUnitEntity {

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

    // Текущее состояние частей тела этого юнита
    @Embedded
    private BodyPartEfficiency bodyEfficiency;

    @Embedded
    private DeflectionChargesEmbeddable deflectionCharges;

    public PlayerUnitEntity(UnitEntity template) {
        this.template = template;

        // Начальные параметры берём из шаблона
        this.maxHealth = template.getMaxHealth();
        this.health = template.getHealth();
        this.damage = template.getDamage();
        this.name = template.getName();
        this.imagePath = template.getImagePath();
        this.bodyEfficiency = template.getBodyEfficiency();
        this.deflectionCharges = template.getDeflectionCharges();
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
}
