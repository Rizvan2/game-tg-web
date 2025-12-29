package org.example.gametgweb.characterSelection.domain.repository;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация доменного репозитория для работы с юнитами игрока
 * <p>
 * Репозиторий инкапсулирует доступ к базе данных через JPA-репозиторий
 * и выполняет преобразование между доменной моделью {@link PlayerUnit}
 * и сущностью хранилища {@link PlayerUnitEntity}.
 */
@Repository
public class PlayerUnitRepositoryImpl implements PlayerUnitRepository {

    private final JpaPlayerUnitRepository jpaPlayerUnitRepository;

    /**
     * Создаёт экземпляр репозитория.
     *
     * @param jpaPlayerUnitRepository JPA-репозиторий для хранения PlayerUnitEntity
     */
    @Autowired
    public PlayerUnitRepositoryImpl(JpaPlayerUnitRepository jpaPlayerUnitRepository) {
        this.jpaPlayerUnitRepository = jpaPlayerUnitRepository;
    }

    /**
     * Ищет юнита игрока по имени.
     *
     * @param name имя юнита (например, "Goblin" или "Elf")
     * @return {@link Optional} с доменной моделью, если найден
     */
    @Override
    public Optional<PlayerUnit> findByName(String name) {
        return jpaPlayerUnitRepository.findByName(name)
                .map(PlayerUnitMapper::toDomain);
    }

    public Optional<PlayerUnit> findById(Long id) {
        return jpaPlayerUnitRepository.findById(id)
                .map(PlayerUnitMapper::toDomain);
    }

    /**
     * Сохраняет или обновляет состояние юнита игрока.
     *
     * @param unit доменная модель юнита
     * @return обновлённая доменная модель, полученная после сохранения
     */
    @Override
    public PlayerUnit save(PlayerUnit unit) {
        PlayerUnitEntity entity = PlayerUnitMapper.toEntity(unit);
        return PlayerUnitMapper.toDomain(jpaPlayerUnitRepository.save(entity));
    }

    /**
     * Возвращает список всех юнитов игрока.
     *
     * @return список доменных моделей
     */
    @Override
    public List<PlayerUnit> findAll() {
        return jpaPlayerUnitRepository.findAll().stream()
                .map(PlayerUnitMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Удаляет юнита по идентификатору.
     *
     * @param id идентификатор юнита игрока
     */
    @Override
    public void deleteById(long id) {
        jpaPlayerUnitRepository.deleteById(id);
    }
}
