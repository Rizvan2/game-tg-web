package org.example.gametgweb.gameplay.game.campaign.controllers;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.game.campaign.entity.Campaign;
import org.example.gametgweb.gameplay.game.entity.player.PlayerDetails;
import org.example.gametgweb.gameplay.game.entity.player.PlayerEntity;
import org.example.gametgweb.services.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер, отвечающий за управление кампаниями (сюжетными боями) игрока.
 * <p>
 * Контроллер принимает запросы от клиента, проверяет аутентификацию игрока
 * и делегирует игровую логику сервису {@link CampaignService}.
 */
@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

    /** Сервис для управления кампаниями и созданием боёв. */
    private final CampaignService campaignService;

    /**
     * Запускает новую кампанию для текущего игрока.
     * <p>
     * Метод проверяет, что пользователь аутентифицирован через {@link PlayerDetails},
     * затем создаёт новую кампанию (бой против заданного противника).
     * <p>
     * В текущей реализации враг фиксирован — "Turk Warrior".
     *
     * @param playerDetails данные о текущем аутентифицированном игроке
     * @return HTTP-ответ со статусом 200 OK и сообщением о начале боя;
     *         если игрок не аутентифицирован — 401 Unauthorized
     */
    @PostMapping("/startCampaign")
    public ResponseEntity<?> startCampaign(@AuthenticationPrincipal PlayerDetails playerDetails) {
        if (playerDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Ошибка аутентификации: игрок не найден");
        }

        PlayerEntity player = playerDetails.playerEntity();
        Campaign campaign = campaignService.startCampaign(player, "Turk Warrior");
        return ResponseEntity.ok("Бой начат против: " + campaign.getEnemyUnitEntity().getName());
    }
}
