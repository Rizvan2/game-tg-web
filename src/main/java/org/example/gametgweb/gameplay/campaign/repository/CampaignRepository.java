package org.example.gametgweb.gameplay.campaign.repository;

import org.example.gametgweb.gameplay.campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
