package org.example.gametgweb.configs.security;

import org.example.gametgweb.gameplay.game.duel.application.services.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 *
 * Используется современный подход без WebSecurityConfigurerAdapter.
 * Настраивает:
 * - авторизацию запросов,
 * - форму логина,
 * - логаут,
 * - хэширование паролей через BCrypt,
 * - AuthenticationManager с использованием кастомного UserDetailsService.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Кастомная реализация UserDetailsService для загрузки игроков из БД */
    private final PlayerServiceImpl userDetailsService;

    @Autowired
    public SecurityConfig(PlayerServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Настройка SecurityFilterChain.
     *
     * @param http HttpSecurity объект для конфигурации фильтров безопасности
     * @return SecurityFilterChain - цепочка фильтров Spring Security
     * @throws Exception если конфигурация невалидна
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register.html", "/login.html").permitAll() // страницы
                        .requestMatchers("/register").permitAll() // POST регистрация
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // статика
                        .anyRequest().authenticated()
                )

                // 2️⃣ Настройка формы логина
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/index.html", true) // ✅ добавь эту строку
                        .failureUrl("/login.html?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                )

                // 3️⃣ Настройка выхода (логаута)
                .logout(LogoutConfigurer::permitAll)
                // Разрешаем выполнение logout всем, иначе только авторизованные смогут разлогиниться

                // 4️⃣ Отключение CSRF
                .csrf(AbstractHttpConfigurer::disable);
        // CSRF (Cross-Site Request Forgery) отключен.
        // Обычно для API на REST это безопасно, для браузерных форм лучше включать.

        // 5️⃣ Построение цепочки фильтров
        return http.build();
        // Возвращает SecurityFilterChain, который Spring Security использует для обработки запросов
    }


    /**
     * Создание AuthenticationManager для Spring Security.
     * Используется кастомный UserDetailsService и BCryptPasswordEncoder.
     *
     * @param http HttpSecurity объект
     * @param passwordEncoder PasswordEncoder для хэширования паролей
     * @return AuthenticationManager
     * @throws Exception если конфигурация невалидна
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        // 1️⃣ Получение билдера аутентификации
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        // AuthenticationManagerBuilder — объект для настройки механизма аутентификации
        // Через него можно подключить кастомный UserDetailsService, энкодеры пароля и другие провайдеры

        // 2️⃣ Настройка кастомного UserDetailsService и энкодера пароля
        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        // userDetailsService — твой PlayerServiceImpl, который загружает данные пользователя из базы
        // passwordEncoder — BCryptPasswordEncoder, проверяет введённый пароль с хранимым

        // 3️⃣ Построение AuthenticationManager
        return authBuilder.build();
        // Возвращает AuthenticationManager, который Spring Security использует для аутентификации пользователей
    }
}
