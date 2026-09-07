package com.company.dragonsofmugloar.domain.game;

/** Snapshot of a game as last reported by the game server, plus who is playing it. */
public record Game(String gameId, int lives, int gold, int level, int score, int highScore, int turn, GameOrigin origin) {

    public boolean isOver() {
        return lives <= 0;
    }

    /** The state after an ad was attempted; the level is untouched by solving. */
    public Game afterSolve(SolveResult result) {
        return new Game(gameId, result.lives(), result.gold(), level, result.score(), result.highScore(), result.turn(), origin);
    }

    /** The state after a shop visit; the score is untouched by buying. */
    public Game afterPurchase(PurchaseResult result) {
        return new Game(gameId, result.lives(), result.gold(), result.level(), score, highScore, result.turn(), origin);
    }
}
