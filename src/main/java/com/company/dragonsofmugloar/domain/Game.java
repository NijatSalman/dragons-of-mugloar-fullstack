package com.company.dragonsofmugloar.domain;

/** Snapshot of a game as last reported by the game server. */
public record Game(String gameId, int lives, int gold, int level, int score, int highScore, int turn) {

    public boolean isOver() {
        return lives <= 0;
    }

    /** The state after an ad was attempted; the level is untouched by solving. */
    public Game afterSolve(SolveResult result) {
        return new Game(gameId, result.lives(), result.gold(), level, result.score(), result.highScore(), result.turn());
    }

    /** The state after a shop visit; the score is untouched by buying. */
    public Game afterPurchase(PurchaseResult result) {
        return new Game(gameId, result.lives(), result.gold(), result.level(), score, highScore, result.turn());
    }
}
