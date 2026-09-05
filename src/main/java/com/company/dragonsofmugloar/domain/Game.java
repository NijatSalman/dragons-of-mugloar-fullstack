package com.company.dragonsofmugloar.domain;

/** Snapshot of a game as last reported by the game server. */
public record Game(String gameId, int lives, int gold, int level, int score, int highScore, int turn) {

    public boolean isOver() {
        return lives <= 0;
    }
}
