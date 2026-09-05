package com.company.dragonsofmugloar.client.dto;

/** Payload exactly as the game server sends it; never leaves the client package. */
public record SolvePayload(boolean success, int lives, int gold, int score, int highScore, int turn, String message) {
}
