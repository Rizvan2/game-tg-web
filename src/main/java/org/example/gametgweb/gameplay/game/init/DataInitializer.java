package org.example.gametgweb.gameplay.game.init;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.entity.Unit;
import org.example.gametgweb.repository.PlayerRepository;
import org.example.gametgweb.repository.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    public CommandLineRunner initUnits(UnitRepository unitRepository) {
        return args -> {
            if (unitRepository.count() == 0) {
                unitRepository.save(Unit.builder()
                        .name("Turk Warrior")
                        .maxHealth(100L)
                        .health(100L)
                        .damage(10L)
                        .imagePath("/images/always-mustachioed.png")
                        .build());
                unitRepository.save(Unit.builder()
                        .name("Goblin")
                        .maxHealth(80L)
                        .health(80L)
                        .damage(8L)
                        .imagePath("/images/player2.png")
                        .build());
            }
        };
    }

    @Bean
    @Order(2)
    public CommandLineRunner initPlayers(PlayerRepository playerRepository, UnitRepository unitRepository, PasswordEncoder encoder) {
        return args -> {
            if (playerRepository.count() == 0) {
                Unit goblin = unitRepository.findByName("Goblin")
                        .orElseThrow(() -> new IllegalStateException("Не найден юнит 'Goblin'"));

                PlayerEntity player1 = new PlayerEntity(encoder.encode("123"), "Нагибатор2017", null);
                PlayerEntity player2 = new PlayerEntity(encoder.encode("123"), "Артьом", goblin);
                PlayerEntity player3 = new PlayerEntity(encoder.encode("123"), "Хакер2013", null);

                playerRepository.save(player1);
                playerRepository.save(player2);
                playerRepository.save(player3);

                System.out.println("✅ Игроки созданы");
            }
        };
    }
}
