package org.example.gametgweb.gameplay.controllers;


import org.example.gametgweb.services.DuelManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DuelController {

    private final DuelManager duelManager;

    @Autowired
    public DuelController(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @PostMapping("/StartDuel")
    public String duel(@RequestParam String gameCode,
                       @RequestParam Long playerId) {

        return duelManager.joinOrCreateGame(gameCode, playerId);
    }
}
