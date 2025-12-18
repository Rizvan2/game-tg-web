package org.example.gametgweb.gameplay.game.duel.domain.repository;

import org.example.gametgweb.gameplay.game.duel.api.dto.GameSessionEntityDto;
import org.example.gametgweb.gameplay.game.duel.domain.model.GameSession;
import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.GameSessionEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.mapper.GameSessionMapper;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaGameSessionRepository;
import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.repository.JpaPlayerRepository;
import org.example.gametgweb.gameplay.game.duel.shared.domain.GameState;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация доменного репозитория {@link GameSessionRepository}, управляющего агрегатом {@link GameSession}.
 * <p>
 * Основная задача репозитория — поддерживать целостность состояния игрового агрегата при взаимодействии
 * игроков с сессией. Репозиторий скрывает детали работы с базой данных через JPA и обеспечивает
 * синхронизацию между доменной моделью, DTO и сущностями.
 *
 * <p>Логика работы с игроками:
 * <ul>
 *     <li>Первый игрок создаёт сессию через {@link #joinOrCreateGame(String, Long)} — создаётся новая доменная модель.</li>
 *     <li>Состояние сессии сохраняется в базу через {@link #save(GameSession)}.</li>
 *     <li>Последующие игроки подтягивают существующую сессию из базы через {@link #findByGameCode(String)}.</li>
 *     <li>Игрок добавляется к доменной модели с помощью {@link #attachPlayerToGame(Long, GameSession)}, проверяя,
 *         что он ещё не в списке игроков и что сессия в состоянии {@link org.example.gametgweb.gameplay.game.duel.shared.domain.GameState#WAITING}.</li>
 *     <li>После добавления нового игрока агрегат синхронизируется с сущностью базы через {@link #updateGame(GameSession)}.</li>
 *     <li>При этом {@link #updateEntityFromDto(GameSessionEntity, GameSessionEntityDto)} получает DTO, содержащий
 *         полный актуальный список игроков (старые и новые), и заменяет коллекцию игроков сущности,
 *         чтобы Hibernate корректно обновил связи в базе.</li>
 * </ul>
 *
 * <p>Таким образом, класс обеспечивает:
 * <ul>
 *     <li>Сохранение новой сессии.</li>
 *     <li>Обновление существующей сессии с добавлением новых игроков без потери старых.</li>
 *     <li>Удаление сессии по идентификатору.</li>
 *     <li>Синхронизацию между доменной моделью, DTO и JPA-сущностью.</li>
 * </ul>
 *
 * <p>Важно: DTO всегда формируется как полное отражение состояния агрегата, что гарантирует,
 * что при обновлении сущности не потеряются игроки, которые уже были сохранены в базе.
 */

@Repository
public class GameSessionRepositoryImpl implements GameSessionRepository {

    private final JpaGameSessionRepository jpaGameSessionRepository;
    private final PlayerRepositoryImpl playerRepository;
    private final JpaPlayerRepository jpaPlayerRepository;


    public GameSessionRepositoryImpl(JpaGameSessionRepository jpaGameSessionRepository,
                                     PlayerRepositoryImpl playerRepository, JpaPlayerRepository jpaPlayerRepository) {
        this.jpaGameSessionRepository = jpaGameSessionRepository;
        this.playerRepository = playerRepository;
        this.jpaPlayerRepository = jpaPlayerRepository;
    }

    /**
     * Ищет игровую сессию по коду.
     *
     * @param gameCode уникальный код сессии
     * @return {@link Optional} с найденной игровой сессией или пустой, если не существует
     */
    @Override
    public Optional<GameSession> findByGameCode(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode)
                .map(GameSessionMapper::toDomain);
    }

    /**
     * Позволяет игроку присоединиться к существующей сессии
     * или создать новую, если она не найдена.
     *
     * @param gameCode уникальный код сессии
     * @param playerId идентификатор игрока
     * @return доменная модель {@link GameSession} после обновления
     * @throws IllegalArgumentException если playerId = null или игрок не найден
     */
    @Override
    @Transactional
    public GameSession joinOrCreateGame(String gameCode, Long playerId) {
        GameSession game = findOrCreateGameSession(gameCode);

        if (playerId == null) {
            throw new IllegalArgumentException("Player ID must not be null");
        }

        attachPlayerToGame(playerId, game);

        updateOrSaveGame(game);

        return game;
    }

    /**
     * Обновляет существующую игровую сессию в базе данных.
     * <p>
     * Находит сущность {@link GameSessionEntity} по идентификатору сессии,
     * затем синхронизирует её поля с DTO {@link GameSessionEntityDto}, созданным из доменной модели.
     * <p>
     * Изменяются только поля, присутствующие в DTO. Игроки синхронизируются:
     * добавляются новые, удаляются отсутствующие, существующие сохраняются.
     *
     * @param game доменная модель {@link GameSession}, содержащая актуальные данные для обновления
     * @throws IllegalStateException если сессия с указанным идентификатором не найдена
     */
    @Override
    @Transactional
    public void updateGame(GameSession game) {
        GameSessionEntityDto dto = GameSessionMapper.toDto(game);

        GameSessionEntity entity = jpaGameSessionRepository
                .findById(dto.id())
                .orElseThrow(() -> new IllegalStateException("Session not found"));

        updateEntityFromDto(entity, dto);
        jpaGameSessionRepository.save(entity);
    }

    /**
     * Сохраняет новую игровую сессию в базе данных.
     * <p>
     * Преобразует доменную модель {@link GameSession} в DTO {@link GameSessionEntityDto},
     * затем обновляет JPA-сущность {@link GameSessionEntity} и сохраняет её через {@link JpaGameSessionRepository}.
     * Все поля сессии, включая игроков, синхронизируются с сущностью.
     *
     * @param game доменная модель {@link GameSession}, содержащая данные для сохранения
     */
    @Override
    @Transactional
    public void save(GameSession game) {
        GameSessionEntityDto entityDto = GameSessionMapper.toDto(game);
        GameSessionEntity gameSessionEntity = new GameSessionEntity();
        updateEntityFromDto(gameSessionEntity, entityDto);
        jpaGameSessionRepository.save(gameSessionEntity);
    }

    /**
     * Удаляет игровую сессию по идентификатору.
     *
     * @param id идентификатор игровой сессии
     */
    @Override
    @Transactional
    public void deleteGame(Long id) {
        jpaGameSessionRepository.deleteById(id);
    }

    private void updateOrSaveGame(GameSession game) {
        if (game.getId() != null) {
            updateGame(game);
        } else {
            save(game);
        }
    }

    /**
     * Находит существующую сессию по коду или создаёт новую.
     *
     * @param gameCode код сессии
     * @return доменная модель {@link GameSession}
     */
    private GameSession findOrCreateGameSession(String gameCode) {
        return jpaGameSessionRepository.findByGameCode(gameCode)
                .map(GameSessionMapper::toDomain)
                .orElseGet(() -> createNewGameSession(gameCode));
    }

    /**
     * Создаёт новую игровую сессию в состоянии {@link GameState#WAITING}
     * и сохраняет её в базу.
     *
     * @param gameCode код создаваемой игры
     * @return доменная модель {@link GameSession} с установленным ID
     */
    private GameSession createNewGameSession(String gameCode) {
        return new GameSession(gameCode, GameState.WAITING);
    }

    /**
     * Привязывает игрока к сессии, если он ещё не добавлен.
     *
     * @param playerId ID игрока
     * @param game     доменная модель игры {@link GameSession}
     * @throws IllegalArgumentException если игрок не найден
     */
    private void attachPlayerToGame(Long playerId, GameSession game) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        if (!game.getPlayers().contains(player)) {
            game.addPlayer(player);
        }
    }

    /**
     * Обновляет состояние сущности {@link GameSessionEntity} на основе данных из {@link GameSessionEntityDto}.
     * <p>
     * Метод устанавливает код игры, текущее состояние и список игроков.
     * Список игроков преобразуется из DTO в сущности {@link PlayerEntity} через {@link JpaPlayerRepository}.
     * Если какой-либо игрок не найден в базе, будет выброшено {@link IllegalArgumentException}.
     *
     * @param entity сущность {@link GameSessionEntity}, которую нужно обновить
     * @param dto    DTO {@link GameSessionEntityDto}, содержащий актуальные данные для обновления
     * @throws IllegalArgumentException если игрок из DTO не найден в базе
     */
    private void updateEntityFromDto(GameSessionEntity entity, GameSessionEntityDto dto) {
        entity.setGameCode(dto.gameCode());
        entity.setState(dto.state());
        if (dto.players() != null) {
            List<PlayerEntity> playerEntities = dto.players().stream().map(dtoEntity -> jpaPlayerRepository.findById(dtoEntity.id())
                            .orElseThrow(() -> new IllegalArgumentException("Плеер не найден")))
                    .collect(Collectors.toCollection(ArrayList::new));

            entity.setPlayers(playerEntities);
        }
    }


}
