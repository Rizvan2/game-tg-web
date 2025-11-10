package org.example.gametgweb.repository;

import org.example.gametgweb.gameplay.game.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий JPA для сущности {@link Unit}.
 * <p>
 * Позволяет выполнять CRUD-операции и искать юниты по имени.
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {

    /**
     * Находит юнита по его имени.
     *
     * @param name имя юнита
     * @return Optional с {@link Unit}, если найден
     */
    Optional<Unit> findByName(String name);
}
