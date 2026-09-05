package com.company.dragonsofmugloar.service.strategy;

import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.AdRecommendation;
import com.company.dragonsofmugloar.domain.Probability;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Turns the ads on the board into ranked {@link AdRecommendation}s. This is the one place that defines what a
 * good ad is; the UI hints and the autoplayer both rely on it, so they can never disagree.
 *
 * <p>An ad is recommended when all three hold: the odds are at least a {@link Probability#GAMBLE}, it will
 * still be on the board when we act, and it does not ask for theft or kidnapping, which the game punishes
 * through reputation. Expected value never affects the flag; it only orders the ads.
 */
@Component
public class AdRecommender {

    static final double MIN_RECOMMENDED_CHANCE = Probability.GAMBLE.successChance();
    static final int MIN_TURNS_LEFT = 2;
    private static final Pattern REPUTATION_DAMAGING = Pattern.compile("\\b(steal|kidnap)", Pattern.CASE_INSENSITIVE);

    private static final Comparator<AdRecommendation> BEST_FIRST = Comparator
            .comparingDouble(AdRecommendation::expectedValue).reversed()
            .thenComparing(Comparator.comparingDouble(AdRecommendation::successChance).reversed());

    /** Recommends every ad and returns them ordered by expected value, ties broken by the safer ad. O(n log n). */
    public List<AdRecommendation> recommend(List<Ad> ads) {
        return ads.stream().map(this::recommend).sorted(BEST_FIRST).toList();
    }

    public AdRecommendation recommend(Ad ad) {
        double chance = ad.probability().successChance();
        return new AdRecommendation(ad, chance, ad.reward() * chance, isRecommended(ad, chance));
    }

    private static boolean isRecommended(Ad ad, double chance) {
        return chance >= MIN_RECOMMENDED_CHANCE
                && ad.expiresIn() >= MIN_TURNS_LEFT
                && !REPUTATION_DAMAGING.matcher(ad.message()).find();
    }
}
