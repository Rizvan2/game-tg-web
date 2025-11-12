package org.example.gametgweb.gameplay.game.campaign.domain.repository;

import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления сущностями {@link CampaignEntity}.
 * <p>
 * Предоставляет стандартные CRUD-операции и возможность
 * добавления пользовательских запросов при необходимости.
 */
@Repository
public interface CampaignRepository extends JpaRepository<CampaignEntity, Long> {
}
