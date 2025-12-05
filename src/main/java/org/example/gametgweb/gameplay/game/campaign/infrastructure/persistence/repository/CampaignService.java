package org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.mapper.PlayerUnitMapper;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.example.gametgweb.gameplay.game.campaign.domain.repository.CampaignRepository;
import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity.CampaignEntity;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.springframework.stereotype.Service;

/**
 * Тестовая конфигурация одного заданного юнита а не динамичного
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final JpaPlayerUnitRepository jpaUnitRepository;

    /** Запуск новой кампании для игрока
     * @param player игрок начавший компанию
     * @return Компания с игроком и его противником
     */
    public CampaignEntity startCampaign(Player player, String enemyUnitName) {
        // Создаём кампанию с юнитами
        CampaignEntity campaignEntity = CampaignEntity.builder()
                .playerUnitEntity(PlayerUnitMapper.toEntity(getPlayerUnit(player)))
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
    private PlayerUnit getPlayerUnit(Player player) {
        PlayerUnit playerUnitEntity = player.getActiveUnit();
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
    private PlayerUnitEntity getEnemyUnit(String name) {
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
