//package org.example.gametgweb.gameplay.game.duel.infrastructure.webSocket.handler;
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.gametgweb.characterSelection.domain.model.PlayerUnit;
//import org.example.gametgweb.characterSelection.domain.repository.PlayerUnitRepositoryImpl;
//import org.example.gametgweb.gameplay.game.duel.domain.model.Player;
//import org.example.gametgweb.gameplay.game.duel.domain.repository.PlayerRepositoryImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//@Slf4j
//public class DuelResultHandler {
//
//    private final PlayerUnitRepositoryImpl unitRepository;
//    private final PlayerRepositoryImpl playerRepository;
//
//    @Autowired
//    public DuelResultHandler(PlayerUnitRepositoryImpl unitRepository, PlayerRepositoryImpl playerRepository) {
//        this.unitRepository = unitRepository;
//        this.playerRepository = playerRepository;
//    }
//
//    @Transactional
//    public void saveLiveUnit(String playerName, PlayerUnit duelRoomUnit) {
//        Player winPlayer = playerRepository.findByUsername(playerName);
//        PlayerUnit winUnit = unitRepository.findByName(duelRoomUnit.getName())
//                .orElseThrow(() -> new IllegalArgumentException("Юнит с таким именем не найден: " + duelRoomUnit.getName()));
//
//        winUnit.setHealth(winUnit.getHealth());
//        winPlayer.setActiveUnit(winUnit);
//        playerRepository.save(winPlayer);
//        log.info("Выживший юнит " + winPlayer.getActiveUnit().getName() + " с здоровьем " + winPlayer.getActiveUnit().getHealth());
//    }
//
//    @Transactional
//    public void saveLoseUnit(String playerName, String duelRoomUnit) {
//        Player losePlayer = playerRepository.findByUsername(playerName);
//        PlayerUnit dropProgressUnit = dropProgress(duelRoomUnit);
//        losePlayer.setActiveUnit(dropProgressUnit);
//        playerRepository.save(losePlayer);
//        log.info("Проигравший юнит " + losePlayer.getActiveUnit().getName() + " с здоровьем " + dropProgressUnit.getHealth());
//    }
//
//    @Transactional(readOnly = true)
//    public PlayerUnit dropProgress(String OldUnit) {
//        return unitRepository.findByName(OldUnit).orElseThrow(() -> new IllegalArgumentException("Юнит с таким именем не найден: " + OldUnit));
//    }
//
//    @Transactional
//    public void calculateDuelResult(String playerName, PlayerUnit duelRoomUnit) {
//        if (duelRoomUnit.isAlive()) {
//            saveLiveUnit(playerName, duelRoomUnit);
//        } else {
//            saveLoseUnit(playerName, duelRoomUnit.getName());
//        }
//    }
//}
