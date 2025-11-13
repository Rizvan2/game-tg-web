package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.combat;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.UnitEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CombatEngine {

    /**
     * Выполняет атаку одного юнита на другого.
     * 
     * @param attacker атакующий юнит
     * @param defender защищающийся юнит
     * @param target часть тела, в которую идёт удар
     * @return текстовое описание хода
     */
    public String performAttack(UnitEntity attacker, UnitEntity defender, Body target) {
        if (attacker == null || defender == null)
            throw new IllegalArgumentException("Attacker или defender не могут быть null");

        long damage = calculateDamage(attacker, defender, target);
        defender.takeDamage(target, damage);

        String message = "%s атакует %s в %s на %d урона".formatted(
                attacker.getName(),
                defender.getName(),
                target.name().toLowerCase(),
                damage
        );

        log.debug("Бой: {}", message);
        return message;
    }

    /**
     * Здесь можно добавить механику критов, брони, уклонения и т.д.
     */
    private long calculateDamage(UnitEntity attacker, UnitEntity defender, Body target) {
        long baseDamage = attacker.getDamage();

        // Пример: шанс критического удара 10%
        if (Math.random() < 0.1) {
            log.debug("💥 Критический удар по {}", target);
            return (long) (baseDamage * 1.5);
        }

        // Пример: броня головы снижает урон
        if (target == Body.HEAD) {
            return (long) (baseDamage * 0.8);
        }

        return baseDamage;
    }
}
