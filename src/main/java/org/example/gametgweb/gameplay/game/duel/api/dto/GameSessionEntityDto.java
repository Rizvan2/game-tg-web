package org.example.gametgweb.gameplay.game.duel.api.dto;

import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;

import java.util.List;

/**
 * DTO для представления игровой сессии и её игроков.
 * <p>
 * Содержит ключевые поля сущности {@link GameSessionEntity}
 * и список игроков в виде {@link PlayerUpdateDto}.
 * <p>
 * Используется для передачи данных между слоем доменной логики и инфраструктурой
 * или для отдачи через API.
 *
 * <ul>
 *     <li>{@code id} — уникальный идентификатор игровой сессии;</li>
 *     <li>{@code gameCode} — код игры или сессии;</li>
 *     <li>{@code state} — текущее состояние игры ({@link GameState});</li>
 *     <li>{@code players} — список игроков с обновляемыми полями в виде {@link PlayerUpdateDto}.</li>
 * </ul>
 */
public record GameSessionEntityDto(Long id, String gameCode, GameState state, List<PlayerUpdateDto> players) {
}
