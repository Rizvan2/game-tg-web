package org.example.gametgweb.characterSelection.infrastructure.persistence.mapper;

import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;

public class UnitMapper {
    /**
     * Преобразует {@link UnitEntity} в доменную модель {@link Unit}.
     *
     * @param entity JPA-сущность юнита
     * @return доменная модель {@link Unit}
     */
    public static Unit toDomain(UnitEntity entity) {
        return new Unit(
                entity.getId(),
                entity.getName(),
                entity.getMaxHealth(),
                entity.getHealth(),
                entity.getDamage(),
                entity.getImagePath()
        );
    }

    /**
     * Преобразует {@link Unit} в {@link UnitEntity}.
     *
     * @param unit доменная модель юнита
     * @return JPA-сущность юнита
     */
    public static UnitEntity toEntity(Unit unit) {
        return new UnitEntity(
                (int) unit.getId(),
                unit.getName(),
                unit.getMaxHealth(),
                unit.getHealth(),
                unit.getDamage(),
                unit.getImagePath()
        );
    }
}
