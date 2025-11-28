package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.characterSelection.domain.model.Unit;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CombatService {

    private final CombatEngine engine;

    public String attack(Unit attacker, Unit defender, Body body) {
        return engine.performAttack(attacker, defender, body);
    }


    /**
     * Выполняет раунд дуэли: оба игрока атакуют друг друга выбранными частями тела.
     *
     * @param attacker первый игрок
     * @param bodyA часть тела, выбранная первым игроком для атаки
     * @param defender второй игрок
     * @param bodyD часть тела, выбранная вторым игроком для атаки
     * @return карта с описанием боя и текущим здоровьем
     */
    public Map<String, Object> duelRound(Unit attacker, Body bodyA, Unit defender, Body bodyD) {
        String msg1 = engine.performAttack(attacker, defender, bodyA);
        String msg2 = engine.performAttack(defender, attacker, bodyD);

        double attackerHpPercent = ((double) attacker.getHealth() / attacker.getMaxHealth()) * 100;
        double defenderHpPercent = ((double) defender.getHealth() / defender.getMaxHealth()) * 100;

        return Map.of(
                "turnMessages", new String[]{msg1, msg2},
                "attackerHp", attackerHpPercent,
                "defenderHp", defenderHpPercent
        );
    }

}
