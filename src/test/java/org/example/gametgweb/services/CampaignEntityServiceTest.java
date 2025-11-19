package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity.CampaignEntity;
import org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.repository.CampaignService;
import org.example.gametgweb.gameplay.game.campaign.domain.repository.CampaignRepository;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaUnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignEntityServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private JpaUnitRepository jpaUnitRepository;

    @InjectMocks
    private CampaignService campaignService;

    @Test
    void startCampaign() {
    }

    @Test
    void saveCampaign() {
        CampaignEntity campaignEntity = new CampaignEntity(4L
                ,new UnitEntity(4L,"Turc Warrior", 100L, 100L,10L,"/images/always-mustachioed.png")
                ,new UnitEntity(4L,"Goblin", 100L, 100L,10L,"/images/Goblin.png"), false);
        campaignService.saveCampaign(campaignEntity);
        // assert - проверяем взаимодействие с мок-репозиторием
        verify(campaignRepository, times(1)).save(campaignEntity);
    }
}
