package org.example.gametgweb.gameplay.game.campaign.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;

/**
 * Сущность, представляющая кампанию (сюжетный бой) игрока.
 * <p>
 * Используется для хранения состояния кампаний в базе данных.
 * Каждая запись описывает конкретное сражение между боевой единицей игрока
 * ({@link #playerUnitEntity}) и вражеским противником ({@link #enemyUnitEntity}).
 */
@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignEntity {

    /** Уникальный идентификатор кампании (генерируется автоматически). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Боевая единица игрока, участвующая в кампании. */
    @ManyToOne(optional = false)
    private PlayerUnitEntity playerUnitEntity;

    /** Вражеский юнит, против которого идёт бой. */
    @ManyToOne(optional = false)
    private PlayerUnitEntity enemyUnitEntity;

    /** Флаг завершённости кампании (true, если бой завершён). */
    @Column(nullable = false)
    private boolean completed = false;
}
