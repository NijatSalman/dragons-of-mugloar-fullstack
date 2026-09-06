package com.company.dragonsofmugloar.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.company.dragonsofmugloar.TestProperties;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.ad.Probability;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Ad texts, ids, rewards and expiry values mirror what the game server really sends. */
class AdRecommenderTest {

    private final AdRecommender recommender = new AdRecommender(TestProperties.autoplay());

    @Test
    void recommendAdWeighsRewardByChance() {
        Ad ad = new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean",
                40, 7, Probability.QUITE_LIKELY);

        AdRecommendation recommendation = recommender.recommendAd(ad);

        assertThat(recommendation.successChance()).isEqualTo(0.80);
        assertThat(recommendation.expectedValue()).isCloseTo(32.0, within(1e-9));
    }

    @Test
    void recommendAdsOrdersByExpectedValueThenBySafety() {
        Ad bigButRisky = new Ad("3haCbU60",
                "Escort Gervase Wheeler to grassland in Frostdinny where they can meet with their long lost chicken",
                100, 7, Probability.RISKY);                                                        // 45
        Ad smallAndSafe = new Ad("HiCtYxHC", "Help Praskoviya Richard to fix their beer mug",
                20, 7, Probability.SURE_THING);                                                   // 20
        Ad sameValueSafer = new Ad("ZsHIk01N", "Help Preecha Saunders to clean their chariot",
                50, 7, Probability.WALK_IN_THE_PARK);                                             // 45

        List<AdRecommendation> result = recommender.recommendAds(List.of(smallAndSafe, bigButRisky, sameValueSafer));

        assertThat(result).extracting(recommendation -> recommendation.ad().adId())
                .containsExactly("ZsHIk01N", "3haCbU60", "HiCtYxHC");
    }

    @Test
    void recommendAdRecommendsGoodOddsEnoughTimeAndHonestWork() {
        Ad ad = new Ad("ulnMLC86", "Help Egídio Holloway to fix their wagon", 3, 2, Probability.GAMBLE);

        assertThat(recommender.recommendAd(ad).recommended()).isTrue();
    }

    @Test
    void recommendAdRejectsOddsWorseThanAGamble() {
        Ad ad = new Ad("wWXQttcJ",
                "Help Helmine Statham to write their biographical novel about their difficulties with a deranged cat",
                30, 7, Probability.RISKY);

        assertThat(recommender.recommendAd(ad).recommended()).isFalse();
    }

    @Test
    void recommendAdRejectsAnAdAboutToExpire() {
        Ad ad = new Ad("72V2caSU", "Help Prakash Osbourne to write their biographical novel about their difficulties "
                + "with a deranged water", 45, 1, Probability.PIECE_OF_CAKE);

        assertThat(recommender.recommendAd(ad).recommended()).isFalse();
    }

    @Test
    void recommendAdRejectsTheftAndKidnappingWhateverTheOdds() {
        Ad theft = new Ad("BcrEjntc", "Steal super awesome diamond from Vendelín Derrickson", 200, 7,
                Probability.SURE_THING);
        Ad kidnapping = new Ad("jWMeTc6h", "Kidnap Blair Bateson's long lost chicken and bring it to Frostdinny",
                200, 7, Probability.SURE_THING);

        assertThat(recommender.recommendAd(theft).recommended()).isFalse();
        assertThat(recommender.recommendAd(kidnapping).recommended()).isFalse();
    }

    @Test
    void recommendAdAcceptsWordsMerelyContainingSteal() {
        Ad ad = new Ad("Ww6aT9xZ", "Help Cassianus Orange to polish their stainless steel armour", 25, 7,
                Probability.SURE_THING);

        assertThat(recommender.recommendAd(ad).recommended()).isTrue();
    }

    @Test
    void recommendAdNeitherRecommendsNorDiscardsUnknownLabels() {
        Ad ad = new Ad("ggLmesXI", "Create an advertisement campaign for Vendelín Derrickson to promote their "
                + "wagon based business", 32, 7, Probability.UNKNOWN);

        AdRecommendation recommendation = recommender.recommendAd(ad);

        assertThat(recommendation.recommended()).isFalse();
        assertThat(recommendation.expectedValue()).isEqualTo(16.0);
    }

    @Test
    void chooseAdPicksTheMostValuableRecommendedAdWhenLivesArePlenty() {
        List<AdRecommendation> board = recommender.recommendAds(List.of(
                new Ad("8ZvZ9jRs", "Help Ken'ichi Trengove to promote their horse based business", 61, 7, Probability.HMMM),
                new Ad("KdX1gKEs", "Help Funda Cropper to sell an unordinary house on the local market", 34, 7,
                        Probability.PIECE_OF_CAKE)));

        Optional<AdRecommendation> choice = recommender.chooseAd(board, 3, 500);

        assertThat(choice).map(chosen -> chosen.ad().adId()).contains("8ZvZ9jRs");
    }

    @Test
    void chooseAdPicksOnlySafeAdsWhenLivesAreLow() {
        List<AdRecommendation> board = recommender.recommendAds(List.of(
                new Ad("8ZvZ9jRs", "Help Ken'ichi Trengove to promote their horse based business", 61, 7, Probability.HMMM),
                new Ad("KdX1gKEs", "Help Funda Cropper to sell an unordinary house on the local market", 34, 7,
                        Probability.PIECE_OF_CAKE)));

        Optional<AdRecommendation> choice = recommender.chooseAd(board, 2, 500);

        assertThat(choice).map(chosen -> chosen.ad().adId()).contains("KdX1gKEs");
    }

    @Test
    void chooseAdPicksTheSafestRecommendedAdWhenLivesAreLowAndNoneIsSafe() {
        List<AdRecommendation> board = recommender.recommendAds(List.of(
                new Ad("8ZvZ9jRs", "Help Ken'ichi Trengove to promote their horse based business", 61, 7, Probability.HMMM),
                new Ad("Vh8MgCru", "Help Päivä Braddock to transport a magic pot to field in Oldwater", 8, 7,
                        Probability.GAMBLE)));

        Optional<AdRecommendation> choice = recommender.chooseAd(board, 1, 500);

        assertThat(choice).map(chosen -> chosen.ad().adId()).contains("8ZvZ9jRs");
    }

    @Test
    void chooseAdReturnsEmptyWhenNothingIsRecommended() {
        List<AdRecommendation> board = recommender.recommendAds(List.of(
                new Ad("NAiJlGN2", "Help defending palace in Loweburg from the intruders", 132, 7, Probability.IMPOSSIBLE),
                new Ad("58rrbJ0D", "Help defending bog in Ferndinny from the intruders", 103, 7, Probability.IMPOSSIBLE)));

        assertThat(recommender.chooseAd(board, 3, 500)).isEmpty();
    }

    @Test
    void chooseAdPicksOnlySafeAdsWhileAPotionIsUnaffordable() {
        List<AdRecommendation> board = recommender.recommendAds(List.of(
                new Ad("qLAvRBYJ", "Help Ken'ichi Trengove to promote their horse based business", 61, 7, Probability.HMMM),
                new Ad("PxlzxCA6", "Help Funda Cropper to sell an unordinary house on the local market", 22, 7,
                        Probability.SURE_THING)));

        Optional<AdRecommendation> choice = recommender.chooseAd(board, 3, 40);

        assertThat(choice).map(chosen -> chosen.ad().adId()).contains("PxlzxCA6");
    }

    @Test
    void chooseAdReturnsEmptyWhenBoardIsEmpty() {
        assertThat(recommender.chooseAd(List.of(), 3, 500)).isEmpty();
    }
}
