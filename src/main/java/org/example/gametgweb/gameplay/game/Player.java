package org.example.gametgweb.gameplay.game;

import lombok.Getter;
import lombok.Setter;

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
    private String nickname;

    /** Ссылка на игру, в которой участвует игрок */
    private final Game game;

    /** ID активного юнита, которым игрок управляет прямо сейчас */
    private Long activeUnitId;

    public Player(Long id, String nickname, Game game, Long activeUnitId) {
        this.id = id;
        this.nickname = nickname;
        this.game = game;
        this.activeUnitId = activeUnitId;
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
