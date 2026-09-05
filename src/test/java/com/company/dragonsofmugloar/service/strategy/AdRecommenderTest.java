package com.company.dragonsofmugloar.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.AdRecommendation;
import com.company.dragonsofmugloar.domain.Probability;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Ad texts, ids, rewards and expiry values mirror what the game server really sends. */
class AdRecommenderTest {

    private final AdRecommender recommender = new AdRecommender();

    @Test
    void expectedValueIsRewardWeightedByChance() {
        Ad ad = new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean",
                40, 7, Probability.QUITE_LIKELY);

        AdRecommendation recommendation = recommender.recommend(ad);

        assertThat(recommendation.successChance()).isEqualTo(0.80);
        assertThat(recommendation.expectedValue()).isCloseTo(32.0, within(1e-9));
    }

    @Test
    void adsAreOrderedByExpectedValueThenBySafety() {
        Ad bigButRisky = new Ad("3haCbU60",
                "Escort Gervase Wheeler to grassland in Frostdinny where they can meet with their long lost chicken",
                100, 7, Probability.RISKY);                                                        // 45
        Ad smallAndSafe = new Ad("HiCtYxHC", "Help Praskoviya Richard to fix their beer mug",
                20, 7, Probability.SURE_THING);                                                   // 20
        Ad sameValueSafer = new Ad("ZsHIk01N", "Help Preecha Saunders to clean their chariot",
                50, 7, Probability.WALK_IN_THE_PARK);                                             // 45

        List<AdRecommendation> result = recommender.recommend(List.of(smallAndSafe, bigButRisky, sameValueSafer));

        assertThat(result).extracting(recommendation -> recommendation.ad().adId())
                .containsExactly("ZsHIk01N", "3haCbU60", "HiCtYxHC");
    }

    @Test
    void goodOddsEnoughTimeAndHonestWorkIsRecommended() {
        Ad ad = new Ad("ulnMLC86", "Help Egídio Holloway to fix their wagon", 3, 2, Probability.GAMBLE);

        assertThat(recommender.recommend(ad).recommended()).isTrue();
    }

    @Test
    void worseOddsThanAGambleAreNotRecommended() {
        Ad ad = new Ad("wWXQttcJ",
                "Help Helmine Statham to write their biographical novel about their difficulties with a deranged cat",
                30, 7, Probability.RISKY);

        assertThat(recommender.recommend(ad).recommended()).isFalse();
    }

    @Test
    void anAdAboutToExpireIsNotRecommended() {
        Ad ad = new Ad("72V2caSU", "Help Prakash Osbourne to write their biographical novel about their difficulties "
                + "with a deranged water", 45, 1, Probability.PIECE_OF_CAKE);

        assertThat(recommender.recommend(ad).recommended()).isFalse();
    }

    @Test
    void theftAndKidnappingAreNotRecommendedWhateverTheOdds() {
        Ad theft = new Ad("BcrEjntc", "Steal super awesome diamond from Vendelín Derrickson", 200, 7,
                Probability.SURE_THING);
        Ad kidnapping = new Ad("jWMeTc6h", "Kidnap Blair Bateson's long lost chicken and bring it to Frostdinny",
                200, 7, Probability.SURE_THING);

        assertThat(recommender.recommend(theft).recommended()).isFalse();
        assertThat(recommender.recommend(kidnapping).recommended()).isFalse();
    }

    @Test
    void wordsMerelyContainingStealAreFine() {
        Ad ad = new Ad("Ww6aT9xZ", "Help Cassianus Orange to polish their stainless steel armour", 25, 7,
                Probability.SURE_THING);

        assertThat(recommender.recommend(ad).recommended()).isTrue();
    }

    @Test
    void unknownLabelsAreNeitherRecommendedNorDiscarded() {
        Ad ad = new Ad("ggLmesXI", "Create an advertisement campaign for Vendelín Derrickson to promote their "
                + "wagon based business", 32, 7, Probability.UNKNOWN);

        AdRecommendation recommendation = recommender.recommend(ad);

        assertThat(recommendation.recommended()).isFalse();
        assertThat(recommendation.expectedValue()).isEqualTo(16.0);
    }
}
