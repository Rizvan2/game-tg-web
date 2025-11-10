package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;

import java.util.Optional;

/**
 * Сервис для работы с игроками (PlayerEntity).
 * <p>
 * Предоставляет методы для поиска, сохранения, обновления и удаления игроков,
 * а также для получения игрока по имени пользователя.
 */
public interface PlayerService {

    /**
     * Находит игрока по его идентификатору.
     *
     * @param id идентификатор игрока
     * @return Optional с PlayerEntity, если найден
     */
    Optional<PlayerEntity> findById(Long id);

    /**
     * Сохраняет нового игрока или обновляет существующего.
     *
     * @param player объект PlayerEntity для сохранения
     * @return сохранённый PlayerEntity
     */
    PlayerEntity savePlayer(PlayerEntity player);

    /**
     * Удаляет игрока из базы данных.
     *
     * @param player объект PlayerEntity для удаления
     */
    void deletePlayer(PlayerEntity player);

    /**
     * Обновляет данные игрока.
     *
     * @param player объект PlayerEntity с обновлёнными данными
     * @return обновлённый PlayerEntity
     */
    PlayerEntity updatePlayer(PlayerEntity player);

    /**
     * Устанавливает состояние игрока.
     * <p>
     * Используется для бизнес-логики, где требуется модификация состояния игрока.
     *
     * @param player объект PlayerEntity
     * @return PlayerEntity после установки состояния
     */
    PlayerEntity setPlayer(PlayerEntity player);

    /**
     * Находит игрока по имени пользователя (username).
     *
     * @param username имя пользователя
     * @return PlayerEntity с указанным именем
     */
    PlayerEntity findByUsername(String username);
}
