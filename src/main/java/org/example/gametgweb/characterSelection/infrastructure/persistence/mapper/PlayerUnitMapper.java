package org.example.gametgweb.characterSelection.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;

/**
 * Маппит состояние юнита конкретного игрока
 */
public class PlayerUnitMapper {
    /**
     * Преобразует {@link PlayerUnitEntity} в доменную модель {@link PlayerUnit}.
     *
     * @param entity JPA-сущность юнита
     * @return доменная модель {@link Unit}
     */
    public static PlayerUnit toDomain(PlayerUnitEntity entity) {
        return new PlayerUnit(
                entity.getId(),
                UnitMapper.toDomain(entity.getTemplate()),
                entity.getName(),
                entity.getMaxHealth(),
                entity.getHealth(),
                entity.getDamage(),
                entity.getImagePath()
        );
    }

    /**
     * Преобразует {@link PlayerUnit} в {@link PlayerUnitEntity}.
     *
     * @param unit доменная модель юнита
     * @return JPA-сущность юнита
     */
    public static PlayerUnitEntity toEntity(PlayerUnit unit) {
        PlayerUnitEntity entity = new PlayerUnitEntity();

        // id — только если есть
        if (unit.getId() != null) {
            entity.setId(unit.getId());
        }

        entity.setTemplate(UnitMapper.toEntity(unit.getTemplate()));
        entity.setName(unit.getName());
        entity.setMaxHealth(unit.getMaxHealth());
        entity.setHealth(unit.getHealth());
        entity.setDamage(unit.getDamage());
        entity.setImagePath(unit.getImagePath());

        return entity;
    }
}
