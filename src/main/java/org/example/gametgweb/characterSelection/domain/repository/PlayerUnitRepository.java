package org.example.gametgweb.characterSelection.domain.repository;

import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления состояния юнита, привязанного к конкретному игроку
 * PlayerUnit — это обертка над базовым Unit, содержащая индивидуальные данные игрока
 * (здоровье, прогресс, выбранные способности и т.п.).
 */
public interface PlayerUnitRepository {

    /**
     * Ищет PlayerUnit по уникальному имени юнита игрока.
     *
     * @param name уникальное имя PlayerUnit
     * @return Optional с найденным PlayerUnit или пустой Optional, если не найден
     */
    Optional<PlayerUnit> findByName(String name);

    /**
     * Возвращает список всех PlayerUnit, существующих в системе.
     *
     * @return список PlayerUnit
     */
    List<PlayerUnit> findAll();

    /**
     * Сохраняет новый PlayerUnit или обновляет существующий.
     *
     * @param unit PlayerUnit для сохранения
     * @return сохранённый экземпляр PlayerUnit
     */
    PlayerUnit save(PlayerUnit unit);

    /**
     * Удаляет PlayerUnit по его идентификатору.
     *
     * @param id идентификатор PlayerUnit
     */
    void deleteById(long id);
}
