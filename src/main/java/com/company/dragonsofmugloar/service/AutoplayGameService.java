package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import com.company.dragonsofmugloar.domain.autoplay.ScoreSummary;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSessionStatus;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameProgress;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameStatus;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.exception.AutoplayGameSessionNotFoundException;
import com.company.dragonsofmugloar.repository.AutoplayGameSessionRepository;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Lets the dragon play several games at once. {@link #startGames} returns immediately; each game sessions on its
 * own virtual thread via {@link GamePlayer#playNewGame} and saves its progress into the session after every turn.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoplayGameService {

    private final GamePlayer gamePlayer;
    private final AutoplayGameSessionRepository sessionRepository;
    private final ExecutorService autoplayExecutor;
    private final Clock clock;

    /** Starts {@code gameCount} games in the background and returns the session that tracks them. */
    public AutoplayGameSession startGames(int gameCount) {
        AutoplayGameSession session = createSession(gameCount);
        for (int gameIndex = 0; gameIndex < gameCount; gameIndex++) {
            playGameInBackground(session.sessionId(), gameIndex);
        }
        return session;
    }

    private AutoplayGameSession createSession(int gameCount) {
        AutoplayGameSession session = AutoplayGameSession.create(UUID.randomUUID().toString(), gameCount, clock.instant());
        sessionRepository.save(session);
        log.info("Autoplay session started: sessionId={}, games={}", session.sessionId(), gameCount);
        return session;
    }

    private void playGameInBackground(String sessionId, int gameIndex) {
        autoplayExecutor.submit(() -> playOneGame(sessionId, gameIndex));
    }

    private void playOneGame(String sessionId, int gameIndex) {
        try {
            Game finished = gamePlayer.playNewGame(game -> saveRunningGame(sessionId, gameIndex, game));
            saveFinishedGame(sessionId, gameIndex, finished);
        } catch (RuntimeException failure) {
            saveFailedGame(sessionId, gameIndex, failure);
        }
    }

    /** The session started by {@code startGames}, with the current state of every game in it. */
    public AutoplayGameSession getGamesProgress(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new AutoplayGameSessionNotFoundException(sessionId));
    }

    private void saveRunningGame(String sessionId, int gameIndex, Game game) {
        saveGameProgress(sessionId, gameIndex, AutoplayGameProgress.fromGame(game, AutoplayGameStatus.RUNNING));
    }

    private void saveFinishedGame(String sessionId, int gameIndex, Game game) {
        saveGameProgress(sessionId, gameIndex, AutoplayGameProgress.fromGame(game, AutoplayGameStatus.FINISHED));
    }

    private void saveGameProgress(String sessionId, int gameIndex, AutoplayGameProgress outcome) {
        sessionRepository.update(sessionId, session -> session.withGame(gameIndex, outcome)).ifPresent(this::logWhenFinished);
    }

    private void saveFailedGame(String sessionId, int gameIndex, RuntimeException failure) {
        log.error("Autoplay game failed: sessionId={}, gameIndex={}, reason={}", sessionId, gameIndex, failure.getMessage(), failure);
        sessionRepository.update(sessionId, session -> session.withFailedGame(gameIndex, failure.getMessage())).ifPresent(this::logWhenFinished);
    }

    /** Logged once per session: the update is atomic, so only the game that ends last sees the session turn finished. */
    private void logWhenFinished(AutoplayGameSession session) {
        if (session.status() != AutoplayGameSessionStatus.FINISHED) {
            return;
        }
        long failedGames = session.games().stream().filter(game -> game.status() == AutoplayGameStatus.FAILED).count();
        ScoreSummary scores = session.scoreSummary().orElse(new ScoreSummary(0, 0, 0));
        log.info("Autoplay session finished: sessionId={}, games={}, failed={}, minScore={}, avgScore={}, maxScore={}",
                session.sessionId(), session.games().size(), failedGames, scores.min(), scores.avg(), scores.max());
    }
}
