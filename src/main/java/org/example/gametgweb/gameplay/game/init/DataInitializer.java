package org.example.gametgweb.gameplay.game.init;

import jakarta.transaction.Transactional;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaUnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    public CommandLineRunner initUnits(JpaUnitRepository jpaUnitRepository) {
        return args -> {
            if (jpaUnitRepository.count() == 0) {
                jpaUnitRepository.save(UnitEntity.builder()
                        .name("Turk Warrior")
                        .maxHealth(100L)
                        .health(100L)
                        .damage(10L)
                        .imagePath("/images/always-mustachioed.png")
                        .build());
                jpaUnitRepository.save(UnitEntity.builder()
                        .name("Goblin")
                        .maxHealth(80L)
                        .health(80L)
                        .damage(8L)
                        .imagePath("/images/Goblin.png")
                        .build());
                jpaUnitRepository.save(UnitEntity.builder()
                        .name("Elf")
                        .maxHealth(120L)
                        .health(120L)
                        .damage(12L)
                        .imagePath("/images/Elf.png")
                        .build());
                jpaUnitRepository.save(UnitEntity.builder()
                        .name("Monk")
                        .maxHealth(100L)
                        .health(100L)
                        .damage(8L)
                        .imagePath("/images/Monk.png")
                        .build());
            }
        };
    }

    @Transactional
    @Bean
    @Order(2)
    public CommandLineRunner initPlayers(JpaPlayerRepository jpaPlayerRepository, JpaUnitRepository jpaUnitRepository, PasswordEncoder encoder) {
        return args -> {
            if (jpaPlayerRepository.count() == 0) {
                UnitEntity goblin = jpaUnitRepository.findByName("Goblin")
                        .orElseThrow(() -> new IllegalStateException("Не найден юнит 'Goblin'"));
                UnitEntity turkWarrior = jpaUnitRepository.findByName("Turk Warrior")
                        .orElseThrow(() -> new IllegalStateException("Не найден юнит 'Turk Warrior'"));

                PlayerEntity player1 = new PlayerEntity(encoder.encode("123"), "Нагибатор2017", null);
                PlayerEntity player2 = new PlayerEntity(encoder.encode("123"), "Артьом", goblin);
                PlayerEntity player3 = new PlayerEntity(encoder.encode("123"), "Хакер2013", turkWarrior);

                jpaPlayerRepository.save(player1);
                jpaPlayerRepository.save(player2);
                jpaPlayerRepository.save(player3);

                System.out.println("✅ Игроки созданы");
                System.out.println(goblin.getName());
            }
        };
    }
}
