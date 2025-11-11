package org.example.gametgweb.gameplay.game.entity.player;

import lombok.Getter;
import lombok.Setter;
import org.example.gametgweb.gameplay.game.entity.gameSession.GameSessionEntity;
import org.example.gametgweb.gameplay.game.entity.unit.UnitEntity;

import java.util.Objects;

/**
 * Игрок в игре (доменная сущность)
 * <p>
 * Содержит только бизнес-данные, не зависит от базы данных.
 */
@Getter
@Setter
public class Player {

    // ======= Геттеры =======
    /** ID игрока (для связи с Telegram или для внутренней логики) */
    private final Long id;

    /** Никнейм игрока */
    private String username;

    /** Ссылка на игру, в которой участвует игрок */
    private final GameSessionEntity gameSessionEntity;

    /** ID активного юнита, которым игрок управляет прямо сейчас */
    private UnitEntity activeUnitEntity;

    public Player(Long id, String username, GameSessionEntity gameSessionEntity, UnitEntity activeUnitEntity) {
        this.id = id;
        this.username = username;
        this.gameSessionEntity = gameSessionEntity;
        this.activeUnitEntity = activeUnitEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
