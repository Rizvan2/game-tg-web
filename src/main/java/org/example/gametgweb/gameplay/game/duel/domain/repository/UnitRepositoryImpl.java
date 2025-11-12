package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.UnitMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaUnitRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация репозитория {@link UnitRepository} для работы с доменной моделью {@link Unit}.
 * <p>
 * Отвечает за преобразование доменной модели в JPA-сущности и обратно, изоляцию доменной логики
 * от деталей хранения данных и предоставление стандартных операций CRUD.
 */
@Repository
public class UnitRepositoryImpl implements UnitRepository {

    private final JpaUnitRepository jpaUnitRepository;

    public UnitRepositoryImpl(JpaUnitRepository jpaUnitRepository) {
        this.jpaUnitRepository = jpaUnitRepository;
    }

    /**
     * Находит юнита по имени.
     *
     * @param name имя юнита
     * @return {@link Optional} с объектом {@link Unit}, если найден
     */
    @Override
    public Optional<Unit> findByName(String name) {
        return jpaUnitRepository.findByName(name)
                .map(UnitMapper::toDomain);
    }

    /**
     * Сохраняет новый юнит или обновляет существующий.
     *
     * @param unit объект {@link Unit} для сохранения
     * @return сохранённый/обновлённый объект {@link Unit}
     */
    @Override
    public Unit save(Unit unit) {
        UnitEntity entity = UnitMapper.toEntity(unit);
        return UnitMapper.toDomain(jpaUnitRepository.save(entity));
    }

    /**
     * Получает список всех юнитов.
     *
     * @return список всех {@link Unit} в системе
     */
    @Override
    public List<Unit> findAll() {
        return jpaUnitRepository.findAll().stream()
                .map(UnitMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Удаляет юнита по его идентификатору.
     *
     * @param id идентификатор юнита
     */
    @Override
    public void deleteById(long id) {
        jpaUnitRepository.deleteById((int) id);
    }
}
