package org.example.gametgweb.gameplay.Campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.gametgweb.gameplay.game.entity.Unit;

@Entity
@Table(name = "campaigns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Unit playerUnit;

    @ManyToOne(optional = false)
    private Unit enemyUnit; // против кого идёт бой

    @Column(nullable = false)
    private boolean completed = false;
}
