package org.example.gametgweb.gameplay.game.duel.shared.domain;

/**
 * Перечисление частей тела юнита для расчета урона.
 * <p>
 * Каждая часть тела имеет множитель урона, который позволяет
 * увеличивать или уменьшать эффект от атаки в зависимости от места попадания.
 */
public enum Body {

    /** Голова — наносится повышенный урон (x2). */
    HEAD(2.0),

    /** Торс/грудь — стандартный урон (x1). */
    CHEST(1.0),

    /** Левая рука — уменьшенный урон (x0.75). */
    LEFT_ARM(0.75),

    /** Правая рука — уменьшенный урон (x0.75). */
    RIGHT_ARM(0.75),

    /** Левая нога — минимальный урон (x0.5). */
    LEFT_LEG(0.5),

    /** Правая нога — минимальный урон (x0.5). */
    RIGHT_LEG(0.5);

    /** Множитель урона для данной части тела. */
    private final double damageMultiplier;

    /**
     * Конструктор для задания множителя урона части тела.
     *
     * @param damageMultiplier множитель урона
     */
    Body(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    /**
     * Получить множитель урона для этой части тела.
     *
     * @return множитель урона
     */
    public double getDamageMultiplier() {
        return damageMultiplier;
    }
}
