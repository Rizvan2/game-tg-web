package org.example.gametgweb.characterSelection.infrastructure.persistence.mapper;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;

@Slf4j
public class UnitMapper {
    /**
     * Преобразует {@link UnitEntity} в доменную модель {@link Unit}.
     *
     * @param entity JPA-сущность юнита
     * @return доменная модель {@link Unit}
     */
    public static Unit toDomain(UnitEntity entity) {
        log.info("=== Маппинг UnitEntity ===");
        log.info("Имя юнита: {}", entity.getName());
        log.info("bodyEfficiency: {}", entity.getBodyEfficiency());

        if (entity.getBodyEfficiency() != null) {
            log.info("  headEfficiency: {}", entity.getBodyEfficiency().getHeadEfficiency());
            log.info("  torsoEfficiency: {}", entity.getBodyEfficiency().getTorsoEfficiency());
        } else {
            log.warn("⚠️ bodyEfficiency = NULL!");
        }
        return new Unit(
                entity.getId(),
                entity.getName(),
                entity.getMaxHealth(),
                entity.getHealth(),
                entity.getDamage(),
                entity.getImagePath(),
                entity.getBodyEfficiency()
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
                unit.getId(),
                unit.getName(),
                unit.getMaxHealth(),
                unit.getHealth(),
                unit.getDamage(),
                unit.getImagePath(),
                unit.getBodyEfficiency()
        );
    }
}
