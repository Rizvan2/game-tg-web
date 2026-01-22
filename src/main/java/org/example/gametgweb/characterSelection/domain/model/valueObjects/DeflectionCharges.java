package org.example.gametgweb.characterSelection.domain.model.valueObjects;


import org.example.gametgweb.characterSelection.api.dto.DeflectionChargesDTO;
import org.example.gametgweb.characterSelection.domain.model.skills.UnitSkill;

/**
 * Value object, представляющий количество зарядов для отражения атак персонажа.
 * <p>
 * Immutable. Все изменения создают новый объект.
 */
public record DeflectionCharges(int current, int max) implements UnitSkill {

    /**
     * Возвращает уникальное название способности.
     * Используется для идентификации навыка в боевой логике или UI.
     *
     * @return название способности, например "Deflection"
     */
    @Override
    public String getName() {
        return "Deflection";
    }

    /**
     * Проверяет, можно ли активировать способность в текущий момент.
     * Для отражения ударов означает, что есть хотя бы один заряд.
     *
     * @return true, если current > 0, иначе false
     */
    @Override
    public boolean canActivate() {
        return current > 0;
    }

    /**
     * Использует один заряд для отражения атаки.
     *
     * @return новый объект DeflectionCharges с уменьшенным на 1 количеством зарядов
     * @throws IllegalStateException если зарядов нет
     */
    @Override
    public DeflectionCharges activate() {
        if (!canActivate()) {
            throw new IllegalStateException("No deflections left");
        }
        return new DeflectionCharges(current - 1, max);
    }

    /**
     * Создаёт новый объект DeflectionCharges.
     *
     * @param current текущее количество зарядов, должно быть >= 0 и <= max
     * @param max     максимальное количество зарядов, должно быть >= 0
     * @throws IllegalArgumentException если current < 0, max < 0 или current > max
     */
    public DeflectionCharges {
        if (current < 0 || max < 0 || current > max) {
            throw new IllegalArgumentException("Invalid deflection charges: current=" + current + ", max=" + max);
        }
    }

    /**
     * Увеличивает максимальное количество зарядов.
     *
     * @param amount положительное число
     * @return новый объект DeflectionCharges с увеличенным max
     * @throws IllegalArgumentException если amount <= 0
     */
    public DeflectionCharges increaseMax(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return new DeflectionCharges(current, max + amount);
    }

    /**
     * Восстанавливает указанное количество зарядов, не превышая max.
     *
     * @param amount положительное число
     * @return новый объект DeflectionCharges с восстановленными current
     * @throws IllegalArgumentException если amount < 0
     */
    public DeflectionCharges restore(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        int newCurrent = Math.min(current + amount, max);
        return new DeflectionCharges(newCurrent, max);
    }

    /**
     * Преобразует DeflectionCharges в DTO для передачи на клиент.
     *
     * @return DeflectionChargesDTO с current и max
     */
    public DeflectionChargesDTO toDTO() {
        return new DeflectionChargesDTO(current, max);
    }
}
