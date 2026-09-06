package com.company.dragonsofmugloar.exception;

/** The game server is unreachable, timed out or failed internally. Mapped to HTTP 502. */
public class GameApiException extends RuntimeException {

    public GameApiException(String message) {
        super(message);
    }

    public GameApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
