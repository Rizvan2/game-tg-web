package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto.DuelRoundResult;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombatService {

    private final CombatEngine engine;

//    public String attack(PlayerUnit attacker, PlayerUnit defender, Body body) {
//        return engine.performAttack(attacker, defender, body);
//    }


    /**
     * Выполняет раунд дуэли: оба игрока атакуют друг друга выбранными частями тела.
     *
     * @param attacker первый игрок
     * @param bodyA часть тела, выбранная первым игроком для атаки
     * @param defender второй игрок
     * @param bodyD часть тела, выбранная вторым игроком для атаки
     * @return карта с описанием боя и текущим здоровьем
     */
    public DuelRoundResult duelRound(PlayerUnit attacker, Body bodyA, PlayerUnit defender, Body bodyD, String gameCode) {
        String msg1 = engine.performAttack(attacker, defender, bodyA, gameCode);
        String msg2 = engine.performAttack(defender, attacker, bodyD, gameCode);

        long attackerHpPercent = (attacker.getHealth() / attacker.getMaxHealth()) * 100;
        long defenderHpPercent = (defender.getHealth() / defender.getMaxHealth()) * 100;

        return new DuelRoundResult(new String[]{msg1, msg2},
                attackerHpPercent,
                defenderHpPercent);
    }

}
