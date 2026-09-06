package com.company.dragonsofmugloar.domain.autoplay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;

/** A request to play several games at once, and how far each of them has got. */
public record AutoplayGameSession(String sessionId, Instant startedAt, List<AutoplayGameProgress> games) {

    public static AutoplayGameSession create(String sessionId, int gameCount, Instant now) {
        return new AutoplayGameSession(sessionId, now, Collections.nCopies(gameCount, AutoplayGameProgress.notStarted()));
    }

    public AutoplayGameSessionStatus status() {
        return games.stream().allMatch(AutoplayGameProgress::hasEnded) ? AutoplayGameSessionStatus.FINISHED : AutoplayGameSessionStatus.RUNNING;
    }

    public long endedGamesCount() {
        return games.stream().filter(AutoplayGameProgress::hasEnded).count();
    }

    /** Empty until at least one game has finished. */
    public Optional<ScoreSummary> scoreSummary() {
        IntSummaryStatistics scores = games.stream()
                .filter(outcome -> outcome.status() == AutoplayGameStatus.FINISHED)
                .mapToInt(AutoplayGameProgress::score)
                .summaryStatistics();
        if (scores.getCount() == 0) {
            return Optional.empty();
        }
        return Optional.of(new ScoreSummary(scores.getMin(), Math.round(scores.getAverage()), scores.getMax()));
    }

    /** A copy of this session with the game at {@code index} marked as failed. */
    public AutoplayGameSession withFailedGame(int index, String reason) {
        return withGame(index, games.get(index).markFailed(reason));
    }

    /** A copy of this session with the game at {@code index} replaced. */
    public AutoplayGameSession withGame(int index, AutoplayGameProgress outcome) {
        List<AutoplayGameProgress> updated = new ArrayList<>(games);
        updated.set(index, outcome);
        return new AutoplayGameSession(sessionId, startedAt, List.copyOf(updated));
    }
}
