package com.company.dragonsofmugloar.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameProgress;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameStatus;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSessionStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AutoplayRunRepositoryTest {

    private static final String SESSION_ID = "6f1c0a3e-8b2d-4c8e-9f1a-2b3c4d5e6f70";

    private final AutoplayGameSessionRepository repository = new AutoplayGameSessionRepository();

    @Test
    void updateReplacesOneGameOfTheSession() {
        repository.save(AutoplayGameSession.create(SESSION_ID, 2, Instant.parse("2026-09-05T14:00:00Z")));

        repository.update(SESSION_ID, session -> session.withGame(1, AutoplayGameProgress.fromGame(new Game("0NVG7E0r", 0, 87, 3, 1462, 1462, 41, GameOrigin.MANUAL), AutoplayGameStatus.FINISHED)));

        AutoplayGameSession session = repository.findById(SESSION_ID).orElseThrow();
        assertThat(session.games().get(0).status()).isEqualTo(AutoplayGameStatus.RUNNING);
        assertThat(session.games().get(1).score()).isEqualTo(1462);
        assertThat(session.status()).isEqualTo(AutoplayGameSessionStatus.RUNNING);
    }

    @Test
    void updateDoesNothingWhenSessionIsUnknown() {
        repository.update(SESSION_ID, session -> session);

        assertThat(repository.findById(SESSION_ID)).isEmpty();
    }
}
