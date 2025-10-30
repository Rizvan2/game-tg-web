package org.example.gametgweb.services;

import org.example.gametgweb.gameplay.game.entity.PlayerDetails;
import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Сервис для управления игроками (PlayerEntity) и интеграции с Spring Security.
 * Реализует интерфейс {@link UserDetailsService} для поддержки аутентификации.
 */
@Service
public class PlayerServiceImpl implements PlayerService, UserDetailsService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Получает игрока по его идентификатору.
     *
     * @param id идентификатор игрока.
     * @return Optional с PlayerEntity, если найден.
     */
    @Override
    public Optional<PlayerEntity> getPlayer(Long id) {
        return playerRepository.findById(id);
    }

    /**
     * Сохраняет нового игрока в базу данных.
     *
     * @param player объект PlayerEntity для сохранения.
     * @return сохранённый экземпляр PlayerEntity.
     */
    @Override
    public PlayerEntity savePlayer(PlayerEntity player) {
        // Хешируем пароль перед сохранением
        player.setPassword(passwordEncoder.encode(player.getPassword()));
        return playerRepository.save(player);
    }

    /**
     * Удаляет игрока из базы данных.
     *
     * @param player объект PlayerEntity, который нужно удалить.
     */
    @Override
    public void deletePlayer(PlayerEntity player) {
        playerRepository.delete(player);
    }

    /**
     * Обновляет данные игрока.
     * (Метод пока не реализован)
     *
     * @param player объект PlayerEntity с обновлёнными данными.
     * @return обновлённый PlayerEntity.
     */
    @Override
    public PlayerEntity updatePlayer(PlayerEntity player) {
        return null;
    }

    /**
     * Устанавливает состояние игрока.
     * (Метод пока не реализован)
     *
     * @param player объект PlayerEntity.
     * @return PlayerEntity после установки состояния.
     */
    @Override
    public PlayerEntity setPlayer(PlayerEntity player) {
        return null;
    }

    /**
     * Метод, используемый Spring Security для загрузки данных пользователя по имени.
     *
     * @param username имя пользователя (логин).
     * @return объект {@link PlayerDetails}, содержащий данные для аутентификации.
     * @throws UsernameNotFoundException если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PlayerEntity player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Игрок не найден: " + username));
        return new PlayerDetails(player);
    }
}
