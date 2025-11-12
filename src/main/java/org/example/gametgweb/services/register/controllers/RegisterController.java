package org.example.gametgweb.services.register.controllers;

import org.example.gametgweb.gameplay.game.duel.infrastructure.persistence.entity.PlayerEntity;
import org.example.gametgweb.gameplay.game.duel.application.services.PlayerService;
import org.example.gametgweb.services.register.dto.PlayerRegisterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

    private final PlayerService playerService;

    @Autowired
    public RegisterController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody PlayerRegisterDto playerRegisterDto) {
        if (playerRegisterDto.username() == null || playerRegisterDto.username().isEmpty() ||
                playerRegisterDto.password() == null || playerRegisterDto.password().isEmpty()) {
            return ResponseEntity.badRequest().body("Заполните все поля");
        }

        PlayerEntity player = new PlayerEntity();
        player.setUsername(playerRegisterDto.username());
        player.setPassword(playerRegisterDto.password());
        playerService.savePlayer(player);

        return ResponseEntity.ok("Пользователь успешно зарегистрирован");
    }
}

