package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;

import java.util.List;
import java.util.Optional;

public interface UnitRepository {

    Optional<Unit> findByName(String name);

    List<Unit> findAll();

    Unit save(Unit unit);

    void deleteById(long id);
}
