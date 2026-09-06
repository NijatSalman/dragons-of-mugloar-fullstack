package com.company.dragonsofmugloar.domain.autoplay;

import com.company.dragonsofmugloar.domain.game.Game;

/**
 * Progress of one game inside an autoplay session.
 *
 * @param gameId {@code null} until the game server has issued an id
 * @param error  only set when the game failed
 */
public record AutoplayGameProgress(String gameId, AutoplayGameStatus status, int score, int turn, int lives, int gold,
                                   int level, String error) {

    public static AutoplayGameProgress notStarted() {
        return new AutoplayGameProgress(null, AutoplayGameStatus.RUNNING, 0, 0, 0, 0, 0, null);
    }

    public static AutoplayGameProgress fromGame(Game game, AutoplayGameStatus status) {
        return new AutoplayGameProgress(game.gameId(), status, game.score(), game.turn(), game.lives(), game.gold(),
                game.level(), null);
    }

    public AutoplayGameProgress markFailed(String reason) {
        return new AutoplayGameProgress(gameId, AutoplayGameStatus.FAILED, score, turn, lives, gold, level, reason);
    }

    public boolean hasEnded() {
        return status != AutoplayGameStatus.RUNNING;
    }
}
