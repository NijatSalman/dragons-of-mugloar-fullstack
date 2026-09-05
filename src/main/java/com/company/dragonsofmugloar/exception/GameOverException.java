package com.company.dragonsofmugloar.exception;

/** The game has ended; no further actions are possible. Mapped to HTTP 410. */
public class GameOverException extends RuntimeException {

    public GameOverException(String gameId) {
        super("Game is over: " + gameId);
    }
}
