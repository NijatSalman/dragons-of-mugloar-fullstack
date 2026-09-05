package com.company.dragonsofmugloar.exception;

/**
 * A call to the game server failed. {@link Reason#REJECTED} means the server refused the request, typically an
 * ad or item that no longer exists (mapped to HTTP 409); {@link Reason#UNAVAILABLE} means the server is
 * unreachable, timed out or failed internally (mapped to HTTP 502).
 */
public class GameApiException extends RuntimeException {

    public enum Reason { REJECTED, UNAVAILABLE }

    private final Reason reason;

    public GameApiException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public GameApiException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
