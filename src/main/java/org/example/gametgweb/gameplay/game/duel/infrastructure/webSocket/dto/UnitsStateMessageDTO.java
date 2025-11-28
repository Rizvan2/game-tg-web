package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.dto;

import java.util.List;
public record UnitsStateMessageDTO(
        String type,
        List<UnitStateDTO> units
) {
    public UnitsStateMessageDTO(List<UnitStateDTO> units) {
        this("UNITS_STATE", units);
    }
}
