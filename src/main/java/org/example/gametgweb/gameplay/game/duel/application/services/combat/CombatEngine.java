package org.example.gametgweb.gameplay.game.duel.application.services.combat;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.application.events.BodyPartDestroyedEvent;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CombatEngine {

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CombatEngine(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Выполняет атаку одного юнита на другого.
     *
     * @param attacker атакующий юнит
     * @param defender защищающийся юнит
     * @param target   часть тела, в которую идёт удар
     * @param gameCode код игровой комнаты
     * @return текстовое описание хода
     */
    public String performAttack(PlayerUnit attacker, PlayerUnit defender, Body target, String gameCode) {
        validateAttackParameters(attacker, defender, target);

        long damage = calculateDamage(attacker, target);
        validateDamage(damage);

        double efficiency = applyDamageAndGetEfficiency(defender, target, damage);

        checkAndPublishBodyPartDestroyed(gameCode, defender, target, efficiency);

        return formatAttackMessage(attacker, defender, target, damage);
    }

    /**
     * Валидирует параметры атаки.
     */
    private void validateAttackParameters(PlayerUnit attacker, PlayerUnit defender, Body target) {
        if (attacker == null || defender == null)
            throw new IllegalArgumentException("Attacker или defender не могут быть null");

        if (target == null)
            throw new IllegalArgumentException("Часть тела не может быть null");
    }

    /**
     * Валидирует рассчитанный урон.
     */
    private void validateDamage(long damage) {
        if (damage < 0)
            throw new IllegalArgumentException("Урон не может быть отрицательным");
    }

    /**
     * Применяет урон к защищающемуся и возвращает эффективность повреждённой части тела.
     */
    private double applyDamageAndGetEfficiency(PlayerUnit defender, Body target, long damage) {
        return defender.takeDamage(target, damage);
    }

    /**
     * Проверяет уничтожение части тела и публикует событие если необходимо.
     */
    private void checkAndPublishBodyPartDestroyed(String gameCode, PlayerUnit defender, Body target, double efficiency) {
        if (efficiency == 0.0) {
            log.warn("💀 {} потерял {}", defender.getName(), target);
            eventPublisher.publishEvent(
                    new BodyPartDestroyedEvent(this, gameCode, defender.getName(), target)
            );
        }
    }

    /**
     * Форматирует сообщение о результате атаки.
     */
    private String formatAttackMessage(PlayerUnit attacker, PlayerUnit defender, Body target, long damage) {
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
    private long calculateDamage(PlayerUnit attacker, Body target) {
        long baseDamage = attacker.getDamage();

        // Критический удар 10%
        if (Math.random() < 0.1) {
            log.debug("💥 Критический удар по {}", target);
            return (long) (baseDamage * target.getDamageMultiplier() * 1.5);
        }

        // Используем множитель части тела
        return (long) (baseDamage * target.getDamageMultiplier());
    }
}
