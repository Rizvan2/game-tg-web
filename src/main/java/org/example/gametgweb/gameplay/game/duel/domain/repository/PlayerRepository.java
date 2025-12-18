package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.domain.model.Player;

import java.util.Optional;

/**
 * Доменный репозиторий для работы с агрегатом {@link Player}.
 * <p>
 * Отвечает за поиск, сохранение, обновление и удаление игроков.
 * Репозиторий инкапсулирует детали хранения и обеспечивает чистый доступ
 * к доменной модели.
 */
public interface PlayerRepository {

    /**
     * Находит игрока по идентификатору.
     *
     * @param id идентификатор игрока
     * @return {@link Optional} с доменной моделью {@link Player} или пустой, если игрок не найден
     */
    Optional<Player> findById(Long id);

    /**
     * Сохраняет нового игрока или обновляет существующего.
     * <p>
     * Используется при создании игрока в системе или изменении данных.
     *
     * @param player доменная модель игрока
     * @return сохранённая доменная модель {@link Player}
     */
//    Player save(Player player);

    /**
     * Удаляет игрока из базы данных.
     *
     * @param id уникальный идентификатор игрока
     */
    void delete(long id);

    /**
     * Обновляет данные существующего игрока.
     *
     * @param player доменная модель игрока с обновлёнными данными
     * @return обновлённая доменная модель {@link Player}
     */
    Player update(Player player);

    /**
     * Находит игрока по имени пользователя (username).
     *
     * @param username имя пользователя
     * @return доменная модель {@link Player} с указанным именем
     * @throws IllegalArgumentException если игрок с таким именем не найден
     */
    Player findByUsername(String username);
}
