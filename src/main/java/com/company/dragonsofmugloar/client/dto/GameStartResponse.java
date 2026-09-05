package com.company.dragonsofmugloar.client.dto;

/** Wire format of the game server, not used outside the client package. */
public record GameStartResponse(String gameId, int lives, int gold, int level, int score, int highScore, int turn) {
}
