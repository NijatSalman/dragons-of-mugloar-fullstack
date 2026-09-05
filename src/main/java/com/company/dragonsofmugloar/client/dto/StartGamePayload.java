package com.company.dragonsofmugloar.client.dto;

/** Payload exactly as the game server sends it; never leaves the client package. */
public record StartGamePayload(String gameId, int lives, int gold, int level, int score, int highScore, int turn) {
}
