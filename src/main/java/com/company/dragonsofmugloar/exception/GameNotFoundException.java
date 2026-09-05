package com.company.dragonsofmugloar.exception;

/** No game exists for the given id. Mapped to HTTP 404. */
public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String gameId) {
        super("Game not found: " + gameId);
    }
}
