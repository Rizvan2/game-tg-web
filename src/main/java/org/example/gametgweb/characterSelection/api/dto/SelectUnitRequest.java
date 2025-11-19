package org.example.gametgweb.characterSelection.api.dto;

/**
 * DTO для передачи данных запроса на выбор юнита игроком.
 * <p>
 * Используется при POST-запросе на эндпоинт выбора юнита.
 */
public record SelectUnitRequest(
        /** Имя юнита, выбранного игроком */
        String unitName
) {}
