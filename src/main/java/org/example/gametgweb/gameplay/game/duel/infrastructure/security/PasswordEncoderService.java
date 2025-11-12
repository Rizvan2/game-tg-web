package org.example.gametgweb.gameplay.game.duel.infrastructure.security;

public interface PasswordEncoderService {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
