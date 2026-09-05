package com.company.dragonsofmugloar.client.dto;

/** Wire format of the game server, not used outside the client package. */
public record SolveResponse(boolean success, int lives, int gold, int score, int highScore, int turn, String message) {
}
