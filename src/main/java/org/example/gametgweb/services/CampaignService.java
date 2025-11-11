package org.example.gametgweb.services;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.example.gametgweb.gameplay.game.campaign.repository.CampaignRepository;
import org.example.gametgweb.gameplay.game.entity.player.PlayerEntity;
import org.example.gametgweb.gameplay.game.entity.unit.UnitEntity;
import org.example.gametgweb.repository.UnitRepository;
import org.springframework.stereotype.Service;

/**
 * Тестовая конфигурация одного заданного юнита а не динамичного
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UnitRepository unitRepository;

    /** Запуск новой кампании для игрока
     * @param player игрок начавший компанию
     * @return Компания с игроком и его противником
     */
    public Campaign startCampaign(PlayerEntity player, String enemyUnitName) {
        // Создаём кампанию с юнитами
        Campaign campaign = Campaign.builder()
                .playerUnitEntity(getPlayerUnit(player))
                .enemyUnitEntity(getEnemyUnit(enemyUnitName))
                .build();

        return campaignRepository.save(campaign);
    }

    public void saveCampaign(Campaign campaign) {
        campaignRepository.save(campaign);
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
        return unitRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Не найден юнит" + name));
    }

    /** Завершить кампанию */
    public void completeCampaign(Campaign campaign) {
        campaign.setCompleted(true);
        campaignRepository.save(campaign);
    }
}
