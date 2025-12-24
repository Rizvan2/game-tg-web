package org.example.gametgweb.gameplay.game.duel.application.services;

import lombok.extern.slf4j.Slf4j;
import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
import org.example.gametgweb.gameplay.game.duel.application.events.DuelDrawEvent;
import org.example.gametgweb.gameplay.game.duel.application.events.DuelFinishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DuelDeathDetector {

    private final ApplicationEventPublisher eventPublisher;

    public DuelDeathDetector(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void checkAndPublishDuelResult(String gameCode, PlayerUnit u1, PlayerUnit u2) {

        if (!u1.isAlive() && !u2.isAlive()) {
            eventPublisher.publishEvent(new DuelDrawEvent(gameCode, u1, u2));
            log.info("Оба юнита погибли: {} и {}", u1, u2);
            return;
        }

        if (!u1.isAlive()) {
            eventPublisher.publishEvent(new DuelFinishedEvent(gameCode, u2, u1));
            log.info("Юнит {} победил, юнит {} сбрасывается", u2, u1);
            return;
        }

        if (!u2.isAlive()) {
            eventPublisher.publishEvent(new DuelFinishedEvent(gameCode, u1, u2));
            log.info("Юнит {} победил, юнит {} сбрасывается", u1, u2);
        }
    }
}
