package com.company.dragonsofmugloar;

import com.company.dragonsofmugloar.config.AutoplayProperties;
import java.util.List;

/** The production defaults from application.yaml, for tests that need real thresholds. */
public final class TestProperties {

    private TestProperties() {
    }

    public static AutoplayProperties autoplay() {
        return new AutoplayProperties(0.55, 2, 2, 0.8, "hpot", 50, 150,
                List.of("cs", "gas", "wax", "tricks", "wingpot"), 350,
                List.of("ch", "rf", "iron", "mtrix", "wingpotmax"), 500);
    }
}
