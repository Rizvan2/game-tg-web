package org.example.gametgweb.repository;

import org.example.gametgweb.gameplay.game.entity.unit.UnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий JPA для сущности {@link UnitEntity}.
 * <p>
 * Позволяет выполнять CRUD-операции и искать юниты по имени.
 */
@Repository
public interface UnitRepository extends JpaRepository<UnitEntity, Integer> {

    /**
     * Находит юнита по его имени.
     *
     * @param name имя юнита
     * @return Optional с {@link UnitEntity}, если найден
     */
    Optional<UnitEntity> findByName(String name);
}
