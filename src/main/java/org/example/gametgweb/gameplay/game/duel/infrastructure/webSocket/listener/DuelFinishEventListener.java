package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.application.events.DuelDrawEvent;
import org.example.gametgweb.gameplay.game.duel.application.events.DuelFinishedEvent;
import org.example.gametgweb.gameplay.game.duel.application.services.DuelFinishService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.registry.UnitRegistryService;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.DuelResultNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DuelFinishEventListener — слушатель доменных событий окончания дуэли.
 *
 * <p>Отвечает исключительно за оркестрацию процессов, происходящих
 * после завершения дуэли, и не содержит бизнес-логики.</p>
 *
 * <p>Реагирует на события:
 * <ul>
 *     <li>{@link DuelFinishedEvent} — победа одного из игроков;</li>
 *     <li>{@link DuelDrawEvent} — одновременная гибель обоих игроков.</li>
 * </ul>
 * </p>
 *
 * <p>В рамках обработки событий:
 * <ul>
 *     <li>Определяет имена игроков по их юнитам без обращения к базе данных
 *         (через {@link UnitRegistryService});</li>
 *     <li>Отправляет игрокам результат дуэли через {@link DuelResultNotifier};</li>
 *     <li>Делегирует сохранение состояния юнитов и удаление комнаты
 *         сервису {@link DuelFinishService}.</li>
 * </ul>
 * </p>
 *
 * <p>Класс намеренно остаётся «тонким» и не зависит от деталей реализации
 * WebSocket-сообщений или слоя хранения данных.</p>
 */
@Service
public class DuelFinishEventListener {

    private final DuelFinishService duelFinishService;
    private final UnitRegistryService unitRegistryService;
    private final DuelResultNotifier duelResultNotifier;

    @Autowired
    public DuelFinishEventListener(DuelFinishService duelFinishService,
                                   UnitRegistryService unitRegistryService,
                                   DuelResultNotifier duelResultNotifier) {
        this.duelFinishService = duelFinishService;
        this.unitRegistryService = unitRegistryService;
        this.duelResultNotifier = duelResultNotifier;
    }

    /**
     * Обрабатывает событие завершения дуэли с победителем.
     *
     * <p>Отправляет уведомление о победе и поражении соответствующим игрокам,
     * затем завершает дуэль на уровне доменной логики.</p>
     *
     * @param event событие завершения дуэли с победителем
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    @EventListener
    @Transactional
    public void onDuelFinished(DuelFinishedEvent event) throws JsonProcessingException {
        PlayerUnit winner = event.winner();
        PlayerUnit loser = event.loser();

        String winnerPlayerName = unitRegistryService.resolvePlayer(event.gameCode(), winner);
        String loserPlayerName = unitRegistryService.resolvePlayer(event.gameCode(), loser);

        duelResultNotifier.sendWin(event.gameCode(), winnerPlayerName);
        duelResultNotifier.sendLose(event.gameCode(), loserPlayerName);

        duelFinishService.finishDuel(event.gameCode(), winner, loser);
    }

    /**
     * Обрабатывает событие ничьей, при которой оба игрока погибают.
     *
     * <p>Оба игрока получают уведомление о поражении, после чего
     * выполняется сброс состояния их юнитов и удаление игровой комнаты.</p>
     *
     * @param event событие ничьей в дуэли
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    @EventListener
    @Transactional
    public void onDuelDraw(DuelDrawEvent event) throws JsonProcessingException {
        PlayerUnit loser1 = event.loser1();
        PlayerUnit loser2 = event.loser2();

        String loserPlayerName1 = unitRegistryService.resolvePlayer(event.gameCode(), loser1);
        String loserPlayerName2 = unitRegistryService.resolvePlayer(event.gameCode(), loser2);

        duelResultNotifier.sendLose(event.gameCode(), loserPlayerName1);
        duelResultNotifier.sendLose(event.gameCode(), loserPlayerName2);

        duelFinishService.finishDuelWithDoubleDeath(event.gameCode(), loser1, loser2);
    }
}
