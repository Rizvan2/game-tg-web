package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.example.gametgweb.gameplay.game.duel.shared.domain.Body;

@Embeddable
@Getter
@Setter
public class BodyPartEfficiency {

    @Column(name = "head_efficiency")
    private double headEfficiency;

    @Column(name = "torso_efficiency")
    private double torsoEfficiency;

    @Column(name = "left_arm_efficiency")
    private double leftArmEfficiency;

    @Column(name = "right_arm_efficiency")
    private double rightArmEfficiency;

    @Column(name = "left_leg_efficiency")
    private double leftLegEfficiency;

    @Column(name = "right_leg_efficiency")
    private double rightLegEfficiency;

    // Конструктор со всеми параметрами
    public BodyPartEfficiency(double headEfficiency, double torsoEfficiency,
                              double leftArmEfficiency, double rightArmEfficiency,
                              double leftLegEfficiency, double rightLegEfficiency) {
        this.headEfficiency = headEfficiency;
        this.torsoEfficiency = torsoEfficiency;
        this.leftArmEfficiency = leftArmEfficiency;
        this.rightArmEfficiency = rightArmEfficiency;
        this.leftLegEfficiency = leftLegEfficiency;
        this.rightLegEfficiency = rightLegEfficiency;
    }


    public BodyPartEfficiency( BodyPartEfficiency bodyPartEfficiency) {
        this.headEfficiency = bodyPartEfficiency.headEfficiency;
        this.torsoEfficiency = bodyPartEfficiency.torsoEfficiency;
        this.leftArmEfficiency = bodyPartEfficiency.leftArmEfficiency;
        this.rightArmEfficiency = bodyPartEfficiency.rightArmEfficiency;
        this.leftLegEfficiency = bodyPartEfficiency.leftLegEfficiency;
        this.rightLegEfficiency = bodyPartEfficiency.rightLegEfficiency;
    }

    // Пустой конструктор для JPA
    public BodyPartEfficiency() {
    }


    /**
     * Снижает эффективность части тела на основе полученного урона.
     * <p>
     * Эффективность рассчитывается как процент от максимального здоровья юнита.
     * Например, урон в 50 единиц при maxHealth=100 снизит эффективность на 50%.
     * Минимальное значение эффективности — 0.0 (часть тела полностью выведена из строя).
     *
     * @param bodyPart  часть тела, которая получила урон
     * @param damage    урон, нанесённый части тела
     * @param maxHealth максимальное здоровье юнита (для расчёта процента потери эффективности)
     * @return текущая эффективность части тела после урона (0.0 - 1.0+, где 1.0 = 100%)
     */
    public double reduceEfficiency(Body bodyPart, long damage, long maxHealth) {
        double efficiencyLoss = (double) damage / maxHealth;

        switch (bodyPart) {
            case HEAD -> headEfficiency = applyReduction(headEfficiency, efficiencyLoss);
            case CHEST -> torsoEfficiency = applyReduction(torsoEfficiency, efficiencyLoss);
            case LEFT_ARM -> leftArmEfficiency = applyReduction(leftArmEfficiency, efficiencyLoss);
            case RIGHT_ARM -> rightArmEfficiency = applyReduction(rightArmEfficiency, efficiencyLoss);
            case LEFT_LEG -> leftLegEfficiency = applyReduction(leftLegEfficiency, efficiencyLoss);
            case RIGHT_LEG -> rightLegEfficiency = applyReduction(rightLegEfficiency, efficiencyLoss);
        }

        return getCurrentEfficiency(bodyPart);
    }

    /**
     * Применяет снижение эффективности с учётом минимального порога.
     *
     * @param current текущая эффективность части тела
     * @param loss    величина потери эффективности
     * @return новая эффективность, не меньше 0.0
     */
    private double applyReduction(double current, double loss) {
        return Math.max(current - loss, 0.0);
    }

    /**
     * Получает текущую эффективность указанной части тела.
     *
     * @param bodyPart часть тела для запроса
     * @return текущая эффективность части тела (0.0 - 1.0+)
     */
    private double getCurrentEfficiency(Body bodyPart) {
        return switch (bodyPart) {
            case HEAD -> headEfficiency;
            case CHEST -> torsoEfficiency;
            case LEFT_ARM -> leftArmEfficiency;
            case RIGHT_ARM -> rightArmEfficiency;
            case LEFT_LEG -> leftLegEfficiency;
            case RIGHT_LEG -> rightLegEfficiency;
        };
    }
}