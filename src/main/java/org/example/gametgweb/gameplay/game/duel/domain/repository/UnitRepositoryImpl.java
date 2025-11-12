package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.UnitMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaUnitRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UnitRepositoryImpl implements UnitRepository {

    private final JpaUnitRepository jpaUnitRepository;

    public UnitRepositoryImpl(JpaUnitRepository jpaUnitRepository) {
        this.jpaUnitRepository = jpaUnitRepository;
    }

    @Override
    public Optional<Unit> findByName(String name) {
        return jpaUnitRepository.findByName(name)
                .map(UnitMapper::toDomain);
    }

    @Override
    public Unit save(Unit unit) {
        UnitEntity entity = UnitMapper.toEntity(unit);
        return UnitMapper.toDomain(jpaUnitRepository.save(entity));
    }

    @Override
    public List<Unit> findAll() {
        return jpaUnitRepository.findAll().stream()
                .map(UnitMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(long id) {
        jpaUnitRepository.deleteById((int) id);
    }
}
