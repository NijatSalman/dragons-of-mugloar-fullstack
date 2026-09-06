package com.company.dragonsofmugloar.exception;

import lombok.Getter;

/** No autoplay session exists for the given id. Mapped to HTTP 404. */
@Getter
public class AutoplayGameSessionNotFoundException extends RuntimeException {

    private final String sessionId;

    public AutoplayGameSessionNotFoundException(String sessionId) {
        super("Autoplay session not found: sessionId=" + sessionId);
        this.sessionId = sessionId;
    }
}
