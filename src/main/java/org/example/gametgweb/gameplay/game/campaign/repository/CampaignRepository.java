package org.example.gametgweb.gameplay.game.campaign.repository;

import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
