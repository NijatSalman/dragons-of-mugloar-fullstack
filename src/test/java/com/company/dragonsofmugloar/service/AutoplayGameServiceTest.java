package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameProgress;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameStatus;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSessionStatus;
import com.company.dragonsofmugloar.exception.AutoplayGameSessionNotFoundException;
import com.company.dragonsofmugloar.exception.GameApiException;
import com.company.dragonsofmugloar.repository.AutoplayGameSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutoplayGameServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-05T14:00:00Z");

    @Mock
    private GamePlayer gamePlayer;

    private final AutoplayGameSessionRepository repository = new AutoplayGameSessionRepository();
    private final ExecutorService sameThread = Executors.newSingleThreadExecutor();

    @AfterEach
    void shutDown() {
        sameThread.close();
    }

    @Test
    void runsEveryRequestedGameAndRecordsTheOutcomes() {
        when(gamePlayer.playNewGame(any())).thenReturn(new Game("0NVG7E0r", 0, 87, 3, 1462, 1462, 41));
        AutoplayGameService service = new AutoplayGameService(gamePlayer, repository, sameThread,
                Clock.fixed(NOW, ZoneOffset.UTC));

        AutoplayGameSession started = service.startGames(3);
        sameThread.close();

        assertThat(started.status()).isEqualTo(AutoplayGameSessionStatus.RUNNING);
        AutoplayGameSession finished = service.getGamesProgress(started.sessionId());
        assertThat(finished.status()).isEqualTo(AutoplayGameSessionStatus.FINISHED);
        assertThat(finished.startedAt()).isEqualTo(NOW);
        assertThat(finished.games()).hasSize(3)
                .allSatisfy(outcome -> {
                    assertThat(outcome.status()).isEqualTo(AutoplayGameStatus.FINISHED);
                    assertThat(outcome.score()).isEqualTo(1462);
                });
    }

    @Test
    void aFailingGameIsRecordedWithoutStoppingTheOthers() {
        when(gamePlayer.playNewGame(any()))
                .thenThrow(new GameApiException("Game server failed: gameId=null, status=503"))
                .thenReturn(new Game("0NVG7E0r", 0, 87, 3, 1462, 1462, 41));
        AutoplayGameService service = new AutoplayGameService(gamePlayer, repository, sameThread, Clock.systemUTC());

        AutoplayGameSession started = service.startGames(2);
        sameThread.close();

        AutoplayGameSession finished = service.getGamesProgress(started.sessionId());
        assertThat(finished.status()).isEqualTo(AutoplayGameSessionStatus.FINISHED);
        assertThat(finished.games()).extracting(AutoplayGameProgress::status)
                .containsExactly(AutoplayGameStatus.FAILED, AutoplayGameStatus.FINISHED);
        assertThat(finished.games().get(0).error()).contains("status=503");
    }

    @Test
    void unknownRunIsRejected() {
        AutoplayGameService service = new AutoplayGameService(gamePlayer, repository, sameThread, Clock.systemUTC());

        assertThatThrownBy(() -> service.getGamesProgress("6f1c0a3e-8b2d-4c8e-9f1a-2b3c4d5e6f70"))
                .isInstanceOf(AutoplayGameSessionNotFoundException.class);
    }
}
