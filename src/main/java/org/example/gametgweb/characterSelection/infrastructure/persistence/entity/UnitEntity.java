package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
public class UnitEntity {

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

    @Embedded
    private BodyPartEfficiency bodyEfficiency;

    @Embedded
    private DeflectionChargesEmbeddable deflectionCharges;
}
