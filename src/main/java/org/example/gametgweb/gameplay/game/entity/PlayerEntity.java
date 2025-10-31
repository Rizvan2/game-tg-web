package org.example.gametgweb.gameplay.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

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
    private String  password;

    @Column(nullable = false, unique = true)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameSession gameSession;

    /**
     * ID юнита, которым игрок управляет прямо сейчас.
     * Позволяет хранить выбранного моба без отдельной коллекции всех юнитов.
     */
    @Column(name = "active_unit_id")
    private Long activeUnitId;

    // ====== Конструкторы ======
    public PlayerEntity() {}

    public PlayerEntity(String  password, String username, Long activeUnitId) {
        this.password = password;
        this.username = username;
        this.activeUnitId = activeUnitId;
    }
}
