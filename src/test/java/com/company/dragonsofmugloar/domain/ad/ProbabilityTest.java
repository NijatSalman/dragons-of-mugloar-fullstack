package com.company.dragonsofmugloar.domain.ad;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProbabilityTest {

    @ParameterizedTest
    @EnumSource(value = Probability.class, mode = EnumSource.Mode.EXCLUDE, names = "UNKNOWN")
    void everyServerLabelParsesBackToItself(Probability probability) {
        assertThat(Probability.fromLabel(probability.label())).isEqualTo(probability);
    }

    @ParameterizedTest
    @CsvSource({"piece of cake, PIECE_OF_CAKE", "  Sure thing , SURE_THING", "HMMM...., HMMM"})
    void parsingIgnoresCaseAndSurroundingSpaces(String label, Probability expected) {
        assertThat(Probability.fromLabel(label)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Totally new label", "Unknown"})
    void unrecognisedLabelsBecomeUnknown(String label) {
        assertThat(Probability.fromLabel(label)).isEqualTo(Probability.UNKNOWN);
    }

    @Test
    void chancesDecreaseFromSureThingToImpossible() {
        double[] chances = Arrays.stream(Probability.values())
                .filter(probability -> probability != Probability.UNKNOWN)
                .mapToDouble(Probability::successChance)
                .toArray();
        for (int index = 1; index < chances.length; index++) {
            assertThat(chances[index]).isLessThan(chances[index - 1]);
        }
        assertThat(Probability.SURE_THING.successChance()).isEqualTo(1.0);
        assertThat(Probability.IMPOSSIBLE.successChance()).isEqualTo(0.0);
    }
}
