package org.example.gametgweb.gameplay.Campaign.repository;

import org.example.gametgweb.gameplay.Campaign.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
}
