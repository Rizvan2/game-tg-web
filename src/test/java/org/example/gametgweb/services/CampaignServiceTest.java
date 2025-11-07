package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.Campaign.entity.Campaign;
import org.example.gametgweb.gameplay.Campaign.repository.CampaignRepository;
import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.entity.Unit;
import org.example.gametgweb.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private CampaignService campaignService;

    @Test
    void startCampaign() {
    }

    @Test
    void saveCampaign() {
        Campaign campaign = new Campaign(4L
                ,new Unit(4L,"Turc Warrior", 100L, 100L,10L,"/images/always-mustachioed.png")
                ,new Unit(4L,"Goblin", 100L, 100L,10L,"/images/Goblin.png"), false);
        campaignService.saveCampaign(campaign);
        // assert - проверяем взаимодействие с мок-репозиторием
        verify(campaignRepository, times(1)).save(campaign);
    }
}
