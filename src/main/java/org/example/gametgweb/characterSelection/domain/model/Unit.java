package org.example.gametgweb.characterSelection.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.BodyPartEfficiency;

/**
 * Доменная модель юнита — отражает игровую логику, а не структуру таблицы.
 */
@Getter
@Setter
@Slf4j
public class Unit {

    private final long id;
    private final String name;
    private long maxHealth;
    private long health;
    private final long damage;
    private final String imagePath;
    private BodyPartEfficiency bodyEfficiency;

    public Unit(long id, String name, long maxHealth, long health, long damage, String imagePath, BodyPartEfficiency bodyEfficiency) {
        this.id = id;
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = health;
        this.damage = damage;
        this.imagePath = imagePath;
        this.bodyEfficiency = bodyEfficiency;
    }
}
