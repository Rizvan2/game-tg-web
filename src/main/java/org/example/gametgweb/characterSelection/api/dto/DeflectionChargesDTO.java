package org.example.gametgweb.characterSelection.api.dto;

import org.example.gametgweb.characterSelection.domain.model.valueObjects.DeflectionCharges;

import java.util.Objects;

/**
 * DTO для передачи количества зарядов отражения атак на клиент.
 * <p>
 * Простой immutable объект для сериализации через WebSocket или REST.
 */
public record DeflectionChargesDTO(int current, int max) {

    /**
     * Создаёт DTO из value object DeflectionCharges.
     *
     * @param charges объект DeflectionCharges
     * @return новый DeflectionChargesDTO
     * @throws NullPointerException если charges == null
     */
    public static DeflectionChargesDTO fromValueObject(DeflectionCharges charges) {
        Objects.requireNonNull(charges, "charges must not be null");
        return new DeflectionChargesDTO(charges.current(), charges.max());
    }
}