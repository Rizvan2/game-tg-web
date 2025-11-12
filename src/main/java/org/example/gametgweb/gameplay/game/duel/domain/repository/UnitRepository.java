package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Unit;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с доменной моделью {@link Unit}.
 * <p>
 * Отвечает за поиск, сохранение и удаление игровых юнитов.
 * Работает только с доменными объектами, не привязываясь к деталям хранения (JPA, БД и т.д.).
 */
public interface UnitRepository {

    /**
     * Находит юнита по имени.
     *
     * @param name имя юнита
     * @return {@link Optional} с объектом {@link Unit}, если найден
     */
    Optional<Unit> findByName(String name);

    /**
     * Получает список всех юнитов.
     *
     * @return список всех {@link Unit} в системе
     */
    List<Unit> findAll();

    /**
     * Сохраняет новый юнит или обновляет существующий.
     *
     * @param unit объект {@link Unit} для сохранения
     * @return сохранённый/обновлённый {@link Unit}
     */
    Unit save(Unit unit);

    /**
     * Удаляет юнита по его идентификатору.
     *
     * @param id идентификатор юнита
     */
    void deleteById(long id);
}
