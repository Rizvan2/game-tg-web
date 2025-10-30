package org.example.gametgweb.gameplay.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Упрощённая JPA Entity для игрока в игре.
 * Содержит минимальные поля: telegramId, nickname, связь с игрой и активный юнит.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long telegramId;

    @Column(nullable = false)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id")
    private GameSession game;

    /**
     * ID юнита, которым игрок управляет прямо сейчас.
     * Позволяет хранить выбранного моба без отдельной коллекции всех юнитов.
     */
    @Column(name = "active_unit_id")
    private Long activeUnitId;

    // ====== Конструкторы ======
    public PlayerEntity() {}

    public PlayerEntity(Long telegramId, String nickname, Long activeUnitId) {
        this.telegramId = telegramId;
        this.nickname = nickname;
        this.activeUnitId = activeUnitId;
    }

}
