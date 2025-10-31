package org.example.gametgweb.gameplay.game.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.gametgweb.gameplay.game.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity для хранения игрового матча в PostgreSQL.
 * <p>
 * Эта сущность представляет отдельный матч в Telegram-игре.
 * Содержит код игры, состояние, временные метки.
 */
@Entity
@Table(name = "games")
@Getter
@Setter
public class GameSession {

    /**
     * Уникальный идентификатор игры (Primary Key).
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Уникальный код игры, который используется для подключения игроков.
     * Например, можно давать игрокам код “ABCD1234” для присоединения к матчу.
     */
    @Column(name = "game_code", unique = true, nullable = false)
    private String gameCode;

    /**
     * Текущее состояние игры.
     * Использует enum {@link GameState}, например: WAITING, IN_PROGRESS, FINISHED.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameState state;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerEntity> players = new ArrayList<>();

    /**
     * Конструктор по умолчанию, требуемый JPA.
     */
    public GameSession() {}

    /**
     * Конструктор с основными полями.
     *
     * @param gameCode уникальный код игры
     * @param state текущее состояние игры
     */
    public GameSession(String gameCode, GameState state) {
        this.gameCode = gameCode;
        this.state = state;
    }

    public void setPlayer(PlayerEntity player) {
        this.players.add(player);
    }
}
//    /**
//     * Дата и время создания игры.
//     * Используется для логирования и сортировки матчей.
//     */
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    /**
//     * Дата и время последнего обновления игры.
//     * Например, при добавлении новых игроков или изменении состояния.
//     */
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;