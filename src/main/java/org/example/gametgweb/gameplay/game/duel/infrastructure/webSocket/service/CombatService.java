package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat.CombatEngine;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CombatService {

    private final CombatEngine engine;

    public String attack(UnitEntity attacker, UnitEntity defender, Body body) {
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
    public Map<String, Object> duelRound(UnitEntity attacker, Body bodyA, UnitEntity defender, Body bodyD) {
        // 1️⃣ Атака первого на второго
        String msg1 = engine.performAttack(attacker, defender, bodyA);

        // 2️⃣ Атака второго на первого
        String msg2 = engine.performAttack(defender, attacker, bodyD);

        // 3️⃣ Возвращаем результат, можно добавить HP юнитов
        return Map.of(
                "turnMessages", new String[]{msg1, msg2},
                "attackerHp", attacker.getHealth(),
                "defenderHp", defender.getHealth()
        );
    }
}
