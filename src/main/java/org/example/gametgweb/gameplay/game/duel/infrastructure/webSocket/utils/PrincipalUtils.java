package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

/**
 * Утилитарный класс для работы с объектом {@link Principal}.
 * <p>
 * Содержит статические методы для безопасного извлечения имени пользователя
 * из Principal, используемого в аутентификации Spring Security или других системах безопасности.
 * <p>
 * Класс финальный и не предназначен для инстанцирования.
 */
@Slf4j
public final class PrincipalUtils {

    /** Приватный конструктор, чтобы предотвратить создание экземпляров класса. */
    private PrincipalUtils() {}

    /**
     * Безопасно извлекает имя игрока из объекта {@link Principal}.
     * <p>
     * Логирует следующие ситуации:
     * <ul>
     *     <li>{@code principal} равен {@code null} → предупреждение;</li>
     *     <li>Имя пустое или blank → предупреждение;</li>
     *     <li>Возникло исключение при извлечении имени → ошибка.</li>
     * </ul>
     *
     * @param principal объект Principal, содержащий данные аутентификации
     * @return имя пользователя или {@code null}, если оно недоступно
     */
    public static String resolvePlayerName(Principal principal) {
        if (principal == null) {
            log.warn("Principal равен null — имя игрока недоступно");
            return null;
        }

        try {
            String name = principal.getName();
            if (name == null || name.isBlank()) {
                log.warn("Principal присутствует, но имя пустое или blank");
                return null;
            }
            return name;
        } catch (Exception e) {
            log.error("Ошибка при получении имени из Principal: {}", e.getMessage(), e);
            return null;
        }
    }
}
