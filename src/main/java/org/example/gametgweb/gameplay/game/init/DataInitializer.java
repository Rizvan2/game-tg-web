package org.example.gametgweb.gameplay.game.init;

import jakarta.transaction.Transactional;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.BodyPartEfficiency;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.PlayerUnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.entity.UnitEntity;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaPlayerUnitRepository;
import org.example.gametgweb.characterSelection.infrastructure.persistence.repository.JpaUnitRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Инициализация базовых игровых данных при старте приложения.
 * <p>
 * Данный конфигурационный класс создаёт:
 * 1) Шаблоны юнитов (UnitEntity)
 * 2) Соответствующие им игровые экземпляры для игроков (PlayerUnitEntity)
 * 3) Тестовых игроков с назначенными активными юнитами
 * <p>
 * Инициализация разделена на два этапа:
 * - @Order(1) — создание юнитов и player-unit обёрток
 * - @Order(2) — создание игроков
 */
@Configuration
public class DataInitializer {
    /**
     * Загружает стартовые шаблоны юнитов и создаёт для каждого PlayerUnitEntity.
     * <p>
     * Выполняется только если таблица PlayerUnit пуста.
     * Шаблоны сохраняются в таблицу unit_entity, после чего на их основе
     * создаются player_unit_entity для последующего использования в игре.
     */
    @Bean
    @Order(1)
    public CommandLineRunner initUnits(JpaUnitRepository jpaUnitRepository, JpaPlayerUnitRepository jpaPlayerUnitRepository) {
        return args -> {
            if (jpaPlayerUnitRepository.count() == 0) {

                List<UnitEntity> templates = List.of(
                        UnitEntity.builder()
                                .name("Turk Warrior")
                                .maxHealth(100L)
                                .health(100L)
                                .damage(10L)
                                .imagePath("/images/always-mustachioed.png")
                                .bodyEfficiency(new BodyPartEfficiency(1,
                                        1.3,
                                        1.2,
                                        1.2,
                                        1.2,
                                        1.2))
                                .build(),

                        UnitEntity.builder()
                                .name("Goblin")
                                .maxHealth(80L)
                                .health(80L)
                                .damage(8L)
                                .imagePath("/images/Goblin.png")
                                .bodyEfficiency(new BodyPartEfficiency(0.8,
                                        1,
                                        0.8,
                                        0.8,
                                        0.8,
                                        0.8))
                                .build(),

                        UnitEntity.builder()
                                .name("Elf")
                                .maxHealth(120L)
                                .health(120L)
                                .damage(12L)
                                .imagePath("/images/Elf.png")
                                .bodyEfficiency(new BodyPartEfficiency(1.1,
                                        1.2,
                                        1.2,
                                        1.2,
                                        1.2,
                                        1.2))
                                .build(),

                        UnitEntity.builder()
                                .name("Monk")
                                .maxHealth(100L)
                                .health(100L)
                                .damage(8L)
                                .imagePath("/images/Monk.png")
                                .bodyEfficiency(new BodyPartEfficiency(1.4,
                                        1.3,
                                        1.4,
                                        1.4,
                                        1.3,
                                        1.3))
                                .build()
                );
                jpaUnitRepository.saveAll(templates);

                for (UnitEntity template : templates) {
                    jpaPlayerUnitRepository.save(new PlayerUnitEntity(template));
                }
            }
        };
    }

    /**
     * Создаёт тестовых игроков и назначает им активные юниты.
     * <p>
     * Выполняется только при пустой таблице players.
     * Юниты извлекаются из player_unit_entity, где они были созданы на этапе @Order(1).
     * <p>
     * Используется @Transactional, чтобы все операции записи прошли атомарно.
     */
    @Transactional
    @Bean
    @Order(2)
    public CommandLineRunner initPlayers(
            JpaPlayerRepository jpaPlayerRepository,
            PasswordEncoder encoder,
            JpaPlayerUnitRepository jpaPlayerUnitRepository
    ) {
        return args -> {
            if (jpaPlayerRepository.count() == 0) {

                PlayerUnitEntity goblinUnit = jpaPlayerUnitRepository
                        .findByName("Goblin")
                        .orElseThrow(() -> new IllegalStateException("Goblin не найден"));

                PlayerUnitEntity turkWarriorUnit = jpaPlayerUnitRepository
                        .findByName("Turk Warrior")
                        .orElseThrow(() -> new IllegalStateException("Turk Warrior не найден"));

                PlayerEntity player1 = new PlayerEntity(encoder.encode("123"), "Нагибатор2017", null);
                PlayerEntity player2 = new PlayerEntity(encoder.encode("123"), "Артьом", goblinUnit);
                PlayerEntity player3 = new PlayerEntity(encoder.encode("123"), "Хакер2013", turkWarriorUnit);

                jpaPlayerRepository.save(player1);
                jpaPlayerRepository.save(player2);
                jpaPlayerRepository.save(player3);

                System.out.println("Players created");
            }
        };
    }
}
