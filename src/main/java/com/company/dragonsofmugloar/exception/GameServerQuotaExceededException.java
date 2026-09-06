package com.company.dragonsofmugloar.exception;

/**
 * The game server refused the request with HTTP 429 because our address sent too many requests in the current
 * minute. The request was not processed, so repeating it after a pause is safe; Resilience4j does that.
 */
public class GameServerQuotaExceededException extends GameApiException {

    public GameServerQuotaExceededException(String gameId) {
        super("Game server quota exceeded: gameId=" + gameId);
    }
}
