package org.example.gametgweb.gameplay.game.init;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.repository.PlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initPlayers(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (playerRepository.count() == 0) {
                PlayerEntity player1 = new PlayerEntity(passwordEncoder.encode("123"), "нуб", null);
                PlayerEntity player2 = new PlayerEntity(passwordEncoder.encode("123"), "про", null);
                PlayerEntity player3 = new PlayerEntity(passwordEncoder.encode("123"), "user3", null);

                playerRepository.save(player1);
                playerRepository.save(player2);
                playerRepository.save(player3);

                System.out.println("✅ Созданы 3 тестовых игрока");
            }
        };
    }
}
