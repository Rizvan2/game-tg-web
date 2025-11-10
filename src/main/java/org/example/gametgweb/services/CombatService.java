package org.example.gametgweb.services;

import lombok.RequiredArgsConstructor;
import org.example.gametgweb.gameplay.combat.CombatEngine;
import org.example.gametgweb.gameplay.game.Body;
import org.example.gametgweb.gameplay.game.entity.Unit;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombatService {

    private final CombatEngine engine;

    public String attack(Unit attacker, Unit defender, Body body) {
        return engine.performAttack(attacker, defender, body);
    }
}
