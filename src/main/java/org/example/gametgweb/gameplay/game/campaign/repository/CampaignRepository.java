package org.example.gametgweb.gameplay.game.campaign.repository;

import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления сущностями {@link Campaign}.
 * <p>
 * Предоставляет стандартные CRUD-операции и возможность
 * добавления пользовательских запросов при необходимости.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
