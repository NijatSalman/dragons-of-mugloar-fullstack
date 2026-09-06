package com.company.dragonsofmugloar.domain.ad;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The game server's textual estimate of how likely an ad is to be solved, with the numeric chance we assign to it.
 * The chances are heuristics tuned against real games; they only need to rank ads correctly relative to each other.
 */
public enum Probability {

    SURE_THING("Sure thing", 1.00),
    PIECE_OF_CAKE("Piece of cake", 0.95),
    WALK_IN_THE_PARK("Walk in the park", 0.90),
    QUITE_LIKELY("Quite likely", 0.80),
    HMMM("Hmmm....", 0.65),
    GAMBLE("Gamble", 0.55),
    RISKY("Risky", 0.45),
    RATHER_DETRIMENTAL("Rather detrimental", 0.35),
    PLAYING_WITH_FIRE("Playing with fire", 0.25),
    SUICIDE_MISSION("Suicide mission", 0.10),
    IMPOSSIBLE("Impossible", 0.00),
    /** A label this application has never seen; treated as a coin flip so it is neither preferred nor excluded. */
    UNKNOWN("Unknown", 0.50);

    private static final Map<String, Probability> BY_LABEL = Arrays.stream(values())
            .filter(probability -> probability != UNKNOWN)
            .collect(Collectors.toUnmodifiableMap(probability -> probability.label.toLowerCase(), Function.identity()));

    private final String label;
    private final double successChance;

    Probability(String label, double successChance) {
        this.label = label;
        this.successChance = successChance;
    }

    /** Parses a server label case-insensitively; anything unrecognised becomes {@link #UNKNOWN}. */
    public static Probability fromLabel(String label) {
        if (label == null) {
            return UNKNOWN;
        }
        return BY_LABEL.getOrDefault(label.trim().toLowerCase(), UNKNOWN);
    }

    public String label() {
        return label;
    }

    public double successChance() {
        return successChance;
    }
}
