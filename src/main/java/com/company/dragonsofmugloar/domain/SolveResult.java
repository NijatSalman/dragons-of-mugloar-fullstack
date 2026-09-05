package com.company.dragonsofmugloar.domain;

/** Outcome of attempting one ad. */
public record SolveResult(boolean success, int lives, int gold, int score, int highScore, int turn, String message) {
}
