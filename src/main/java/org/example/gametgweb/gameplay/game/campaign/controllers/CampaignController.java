package org.example.gametgweb.gameplay.game.campaign.controllers;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.example.gametgweb.gameplay.game.entity.PlayerDetails;
import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.services.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping("/startCampaign")
    public ResponseEntity<?> startCampaign(@AuthenticationPrincipal PlayerDetails playerDetails) {
        if (playerDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ошибка аутентификации: игрок не найден");
        }

        PlayerEntity player = playerDetails.playerEntity();
        Campaign campaign = campaignService.startCampaign(player, "Turk Warrior");
        return ResponseEntity.ok("Бой начат против: " + campaign.getEnemyUnit().getName());
    }

}
