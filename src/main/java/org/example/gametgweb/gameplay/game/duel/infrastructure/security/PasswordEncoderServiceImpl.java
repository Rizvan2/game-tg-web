package org.example.gametgweb.gameplay.game.duel.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для кодирования и проверки паролей.
 * <p>
 * Реализация интерфейса {@link PasswordEncoderService} использует
 * {@link PasswordEncoder} от Spring Security для безопасного хранения паролей.
 * <p>
 * В DDD этот сервис находится в инфраструктурном слое, так как реализация
 * зависит от внешней библиотеки и не является частью доменной логики.
 */
@Service
public class PasswordEncoderServiceImpl implements PasswordEncoderService {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Кодирует исходный пароль.
     *
     * @param rawPassword исходный пароль
     * @return закодированный пароль
     */
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Сравнивает исходный пароль с закодированным.
     *
     * @param rawPassword     исходный пароль
     * @param encodedPassword закодированный пароль из базы
     * @return true, если пароли совпадают, иначе false
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
