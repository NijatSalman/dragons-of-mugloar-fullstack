package com.company.dragonsofmugloar.exception;

import lombok.Getter;

/** The ad is no longer on the board, usually solved or expired between listing and solving. Mapped to HTTP 409. */
@Getter
public class AdNotAvailableException extends RuntimeException {

    private final String gameId;
    private final String adId;

    public AdNotAvailableException(String gameId, String adId) {
        super("Ad not available: gameId=" + gameId + ", adId=" + adId);
        this.gameId = gameId;
        this.adId = adId;
    }
}
