package org.example.gametgweb.gameplay.game.duel.domain.exception;

public class GameAlreadyExistsException extends RuntimeException {
    public GameAlreadyExistsException(String gameCode) {
        super("Комната с кодом '" + gameCode + "' уже существует");
    }
}
