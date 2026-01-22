package org.example.gametgweb.characterSelection.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import org.example.gametgweb.characterSelection.domain.model.valueObjects.DeflectionCharges;

@Embeddable
@Getter

/**
 * JPA Embeddable для хранения информации о зарядах отражения (deflection) юнита.
 * <p>
 * Используется внутри {@link org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity}
 * для сохранения текущего и максимального количества зарядов в базе данных.
 * <p>
 * Является обёрткой над доменным Value Object {@link DeflectionCharges} для работы с JPA.
 */
public class DeflectionChargesEmbeddable {
    /**
     * Текущее количество зарядов отражения юнита.
     */
    @Column(name = "deflection_current")
    private int current;
    /**
     * Максимальное количество зарядов отражения юнита.
     */
    @Column(name = "deflection_max")
    private int max;

    protected DeflectionChargesEmbeddable() {}

    public DeflectionChargesEmbeddable(int current, int max) {
        this.current = current;
        this.max = max;
    }
    /**
     * Преобразует данный Embeddable в доменный Value Object {@link DeflectionCharges}.
     *
     * @return новый объект {@link DeflectionCharges} с текущим и максимальным значением зарядов
     */
    public DeflectionCharges toDomain() {
        return new DeflectionCharges(current, max);
    }

    public static DeflectionChargesEmbeddable fromDomain(DeflectionCharges domain) {
        return new DeflectionChargesEmbeddable(
            domain.current(),
            domain.max()
        );
    }
}
