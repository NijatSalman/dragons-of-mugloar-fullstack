package com.company.dragonsofmugloar.observability;

import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Game;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Business metrics of the game, exported through Actuator for Prometheus. HTTP latencies need nothing here:
 * Spring Boot already records {@code http.server.requests} for our API and {@code http.client.requests} for
 * every call to the game server.
 */
@Component
@RequiredArgsConstructor
public class GameMetrics {

    static final String ADS_SOLVED = "mugloar.ads.solved";
    static final String ITEMS_PURCHASED = "mugloar.items.purchased";
    static final String GAME_SCORE = "mugloar.game.score";

    private final MeterRegistry registry;

    /** Counts solve attempts by the server's probability label and whether they succeeded. */
    public void countAdSolved(Probability probability, boolean success) {
        registry.counter(ADS_SOLVED, "probability", probability.name(), "success", Boolean.toString(success)).increment();
    }

    /** Counts purchases by item and whether the server accepted them. */
    public void countItemPurchased(String itemId, boolean success) {
        registry.counter(ITEMS_PURCHASED, "item", itemId, "success", Boolean.toString(success)).increment();
    }

    /** Records the final score of a finished game, so min, average and percentiles can be charted. */
    public void recordFinalScore(Game game) {
        DistributionSummary.builder(GAME_SCORE)
                .description("Final score of finished games")
                .publishPercentiles(0.5, 0.9)
                .register(registry)
                .record(game.score());
    }
}
