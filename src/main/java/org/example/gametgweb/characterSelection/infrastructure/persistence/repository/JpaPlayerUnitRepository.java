package org.example.gametgweb.characterSelection.infrastructure.persistence.repository;

import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPlayerUnitRepository extends JpaRepository<PlayerUnitEntity, Long> {
    /**
     * Находит юнита по его имени.
     *
     * @param name имя юнита
     * @return Optional с {@link UnitEntity}, если найден
     */
    Optional<PlayerUnitEntity> findByName(String name);
}
