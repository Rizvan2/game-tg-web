package org.example.gametgweb.characterSelection.application.services;

import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UnitService {

    private final JpaUnitRepository unitRepository;

    public UnitService(JpaUnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    /** Получить юнита по имени */
    public Optional<UnitEntity> getUnitByName(String name) {
        return unitRepository.findByName(name);
    }

    /** Получить всех юнитов */
    public List<UnitEntity> getAllUnits() {
        return unitRepository.findAll();
    }

    /** Сохранить или обновить юнита */
    @Transactional
    public UnitEntity saveUnit(UnitEntity unit) {
        return unitRepository.save(unit);
    }

    /** Удалить юнита по ID */
    @Transactional
    public void deleteUnit(Integer id) {
        unitRepository.deleteById(id);
    }
}
