package org.example.gametgweb.characterSelection.infrastructure.persistence.repository;

import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий JPA для сущности {@link UnitEntity}.
 * <p>
 * Позволяет выполнять CRUD-операции и искать юниты по имени.
 */
@Repository
public interface JpaUnitRepository extends JpaRepository<UnitEntity, Integer> {

    /**
     * Находит юнита по его имени.
     *
     * @param name имя юнита
     * @return Optional с {@link UnitEntity}, если найден
     */
    Optional<UnitEntity> findByName(String name);
}
