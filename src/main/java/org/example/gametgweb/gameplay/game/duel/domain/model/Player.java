package org.example.gametgweb.gameplay.game.duel.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.example.gametgweb.characterSelection.domain.model.Unit;

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
    private GameSession gameSession;

    /** ID активного юнита, которым игрок управляет прямо сейчас */
    private Unit activeUnit;

    public Player(Long id, String username, GameSession gameSession, Unit activeUnit) {
        this.id = id;
        this.username = username;
        this.gameSession = gameSession;
        this.activeUnit = activeUnit;
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
