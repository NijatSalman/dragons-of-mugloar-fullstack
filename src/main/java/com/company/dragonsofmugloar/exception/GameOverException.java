package com.company.dragonsofmugloar.exception;

import lombok.Getter;

/** The game has ended; no further actions are possible. Mapped to HTTP 410. */
@Getter
public class GameOverException extends RuntimeException {

    private final String gameId;

    public GameOverException(String gameId) {
        super("Game over: gameId=" + gameId);
        this.gameId = gameId;
    }
}
