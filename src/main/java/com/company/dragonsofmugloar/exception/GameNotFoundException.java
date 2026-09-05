package com.company.dragonsofmugloar.exception;

import lombok.Getter;

/** No game exists for the given id, or it has expired on the game server. Mapped to HTTP 404. */
@Getter
public class GameNotFoundException extends RuntimeException {

    private final String gameId;

    public GameNotFoundException(String gameId) {
        super("Game not found: gameId=" + gameId);
        this.gameId = gameId;
    }
}
