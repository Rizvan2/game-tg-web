package org.example.gametgweb.gameplay.game.duel.application.services;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.PlayerMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис управления состоянием игрока в рамках игровой сессии.
 *
 * <p>
 * Отвечает за смену активного юнита игрока с сохранением его состояния,
 * если выбран тот же шаблон юнита.
 * </p>
 *
 * <p>
 * Основная задача сервиса — предотвратить нежелательный сброс состояния
 * (HP, эффекты, кулдауны и т.п.) при повторном выборе того же типа юнита.
 * </p>
 */
@Slf4j
@Service
public class PlayerUnitSelectionService {

    private final JpaPlayerRepository jpaPlayerRepository;
    private final JpaPlayerUnitRepository playerUnitRepository;

    @Autowired
    public PlayerUnitSelectionService(JpaPlayerRepository jpaPlayerRepository, JpaPlayerUnitRepository playerUnitRepository) {
        this.jpaPlayerRepository = jpaPlayerRepository;
        this.playerUnitRepository = playerUnitRepository;
    }

    /**
     * Назначает активного игрового юнита игроку.
     *
     * <ul>
     *   <li>Если у игрока ещё нет активного юнита — выбранный юнит назначается.</li>
     *   <li>Если активный юнит уже существует и его шаблон совпадает
     *       с шаблоном нового юнита — замена не выполняется
     *       (состояние юнита сохраняется).</li>
     *   <li>Если шаблон отличается — активный юнит заменяется.</li>
     * </ul>
     *
     * <p>
     * Такой подход предотвращает сброс состояния текущего юнита
     * при повторном выборе того же шаблона.
     * </p>
     *
     * @param player доменная модель игрока
     * @param unit   доменная модель выбранного игрового юнита
     * @return обновлённая доменная модель игрока
     * @throws IllegalArgumentException если игрок не найден в базе данных
     */
    public Player selectUnitForPlayer(Player player, PlayerUnit unit) {
        PlayerEntity entity = jpaPlayerRepository.findById(player.getId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        log.info(unit.getId().toString());
        log.info("айди вроде найден");
        PlayerUnitEntity playerUnitEntity = playerUnitRepository.findById(unit.getId())
                .orElseGet(() -> PlayerUnitMapper.toEntity(unit));

        /*
         * Если активного юнита нет
         * ИЛИ
         * если шаблон текущего активного юнита отличается от нового,
         * то выполняется замена.
         *
         * В противном случае состояние текущего юнита сохраняется.
         */
        if (entity.getActiveUnitEntity() == null ||
                !entity.getActiveUnitEntity()
                        .getTemplate()
                        .equals(playerUnitEntity.getTemplate())) {

            entity.setActiveUnitEntity(playerUnitEntity);
            jpaPlayerRepository.save(entity);
        }

        return PlayerMapper.toDomain(entity);
    }
}
