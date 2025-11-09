package org.example.gametgweb.gameplay.game.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.gametgweb.gameplay.game.entity.Unit;

/**
 * Сущность, представляющая кампанию (сюжетный бой) игрока.
 * <p>
 * Используется для хранения состояния кампаний в базе данных.
 * Каждая запись описывает конкретное сражение между боевой единицей игрока
 * ({@link #playerUnit}) и вражеским противником ({@link #enemyUnit}).
 */
@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    /** Уникальный идентификатор кампании (генерируется автоматически). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Боевая единица игрока, участвующая в кампании. */
    @ManyToOne(optional = false)
    private Unit playerUnit;

    /** Вражеский юнит, против которого идёт бой. */
    @ManyToOne(optional = false)
    private Unit enemyUnit;

    /** Флаг завершённости кампании (true, если бой завершён). */
    @Column(nullable = false)
    private boolean completed = false;
}
