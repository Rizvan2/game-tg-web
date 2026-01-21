package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.gametgweb.gameplay.game.duel.application.events.BodyPartDestroyedEvent;
import org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.service.BodyPartNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * BodyPartEventListener — слушатель событий, связанных с частями тела юнитов.
 *
 * <p>Отвечает за оркестрацию процессов при изменении состояния частей тела
 * и не содержит бизнес-логики.</p>
 *
 * <p>Реагирует на события:
 * <ul>
 *     <li>{@link BodyPartDestroyedEvent} — уничтожение части тела юнита.</li>
 * </ul>
 * </p>
 *
 * <p>В рамках обработки событий:
 * <ul>
 *     <li>Отправляет игрокам уведомление об уничтожении части тела через {@link BodyPartNotifier}.</li>
 * </ul>
 * </p>
 */
@Service
public class BodyPartEventListener {

    private final BodyPartNotifier bodyPartNotifier;

    @Autowired
    public BodyPartEventListener(BodyPartNotifier bodyPartNotifier) {
        this.bodyPartNotifier = bodyPartNotifier;
    }

    /**
     * Обрабатывает событие уничтожения части тела.
     *
     * <p>Отправляет всем игрокам в комнате уведомление о том, что часть тела была уничтожена.</p>
     *
     * @param event событие уничтожения части тела
     * @throws JsonProcessingException если возникла ошибка сериализации сообщения
     */
    @EventListener
    public void onBodyPartDestroyed(BodyPartDestroyedEvent event) throws JsonProcessingException {
        bodyPartNotifier.sendBodyPartDestroyed(
                event.getGameCode(),
                event.getPlayerName(),
                event.getBodyPart()
        );
    }
}