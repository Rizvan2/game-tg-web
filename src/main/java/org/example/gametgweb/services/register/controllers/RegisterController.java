package org.example.gametgweb.services.register.controllers;

import org.example.gametgweb.gameplay.game.entity.PlayerEntity;
import org.example.gametgweb.services.PlayerService;
import org.example.gametgweb.services.register.dto.PlayerRegisterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class RegisterController {

    private final PlayerService playerService;

    @Autowired
    public RegisterController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public String register(@RequestBody PlayerRegisterDto playerRegisterDto) {
        if (playerRegisterDto.username() == null || playerRegisterDto.username().isEmpty() ||
                playerRegisterDto.password() == null || playerRegisterDto.password().isEmpty()) {
            return "redirect:/register";
        }
        PlayerEntity player = new PlayerEntity();
        player.setUsername(playerRegisterDto.username());
        player.setPassword(playerRegisterDto.password());
        playerService.savePlayer(player);
        // После успешной регистрации — редиректим на главную
        return "redirect:/index.html";
    }
}
