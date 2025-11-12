package org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity.CampaignEntity;
import org.example.gametgweb.gameplay.game.campaign.domain.repository.CampaignRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaUnitRepository;
import org.springframework.stereotype.Service;

/**
 * Тестовая конфигурация одного заданного юнита а не динамичного
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final JpaUnitRepository jpaUnitRepository;

    /** Запуск новой кампании для игрока
     * @param player игрок начавший компанию
     * @return Компания с игроком и его противником
     */
    public CampaignEntity startCampaign(PlayerEntity player, String enemyUnitName) {
        // Создаём кампанию с юнитами
        CampaignEntity campaignEntity = CampaignEntity.builder()
                .playerUnitEntity(getPlayerUnit(player))
                .enemyUnitEntity(getEnemyUnit(enemyUnitName))
                .build();

        return campaignRepository.save(campaignEntity);
    }

    public void saveCampaign(CampaignEntity campaignEntity) {
        campaignRepository.save(campaignEntity);
    }

    /**
     * Достает активного юнита из текущего аутентифицированного игрока
     * @param player текущий аутентифицированный игрок
     * @return Юнит переданного игрока
     * @throws IllegalStateException если у игрока нету выбранного юнита
     */
    private UnitEntity getPlayerUnit(PlayerEntity player) {
        UnitEntity playerUnitEntity = player.getActiveUnitEntity();
        if (playerUnitEntity == null) {
            throw new IllegalStateException("У игрока не выбран активный юнит");
        }
        return playerUnitEntity;
    }

    /**
     * Ищет противостоящего юнита по имени
     * @param name Противник которого мы ищем
     * @return UnitEntity с совпадающим именем
     */
    private UnitEntity getEnemyUnit(String name) {
        // выбираем первого врага (например, по имени)
        return jpaUnitRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Не найден юнит" + name));
    }

    /** Завершить кампанию */
    public void completeCampaign(CampaignEntity campaignEntity) {
        campaignEntity.setCompleted(true);
        campaignRepository.save(campaignEntity);
    }
}
