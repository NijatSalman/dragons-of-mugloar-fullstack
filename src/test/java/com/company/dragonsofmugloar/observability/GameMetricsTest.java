package com.company.dragonsofmugloar.observability;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Game;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class GameMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final GameMetrics metrics = new GameMetrics(registry);

    @Test
    void countAdSolvedCountsByProbabilityAndOutcome() {
        metrics.countAdSolved(Probability.QUITE_LIKELY, true);
        metrics.countAdSolved(Probability.QUITE_LIKELY, true);
        metrics.countAdSolved(Probability.QUITE_LIKELY, false);

        assertThat(registry.get(GameMetrics.ADS_SOLVED).tags("probability", "QUITE_LIKELY", "success", "true")
                .counter().count()).isEqualTo(2);
        assertThat(registry.get(GameMetrics.ADS_SOLVED).tags("probability", "QUITE_LIKELY", "success", "false")
                .counter().count()).isEqualTo(1);
    }

    @Test
    void countItemPurchasedCountsByItemAndOutcome() {
        metrics.countItemPurchased("hpot", true);
        metrics.countItemPurchased("hpot", false);

        assertThat(registry.get(GameMetrics.ITEMS_PURCHASED).tags("item", "hpot", "success", "true")
                .counter().count()).isEqualTo(1);
    }

    @Test
    void recordFinalScoreRecordsEveryFinishedGame() {
        metrics.recordFinalScore(new Game("jz21oOWI", 0, 87, 3, 5239, 5239, 201));
        metrics.recordFinalScore(new Game("8ZW2dZlT", 0, 12, 4, 6159, 6159, 248));

        var summary = registry.get(GameMetrics.GAME_SCORE).summary();
        assertThat(summary.count()).isEqualTo(2);
        assertThat(summary.max()).isEqualTo(6159);
        assertThat(summary.totalAmount()).isEqualTo(5239 + 6159);
    }
}
