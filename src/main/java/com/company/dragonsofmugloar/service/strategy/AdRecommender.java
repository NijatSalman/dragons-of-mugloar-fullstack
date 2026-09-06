package com.company.dragonsofmugloar.service.strategy;

import com.company.dragonsofmugloar.config.AutoplayProperties;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Turns the ads on the board into ranked {@link AdRecommendation}s and picks the one to play. This is the one
 * place that defines what a good ad is; the UI hints and the autoplayer both rely on it, so they never disagree.
 */
@Component
@RequiredArgsConstructor
public class AdRecommender {

    private static final Pattern REPUTATION_DAMAGING = Pattern.compile("\\b(steal|kidnap)", Pattern.CASE_INSENSITIVE);

    private static final Comparator<AdRecommendation> BEST_FIRST = Comparator
            .comparingDouble(AdRecommendation::expectedValue).reversed()
            .thenComparing(Comparator.comparingDouble(AdRecommendation::successChance).reversed());
    private static final Comparator<AdRecommendation> BY_CHANCE = Comparator.comparingDouble(AdRecommendation::successChance);

    private final AutoplayProperties properties;

    /** Recommends every ad and returns them ordered by expected value, ties broken by the safer ad. O(n log n). */
    public List<AdRecommendation> recommendAds(List<Ad> ads) {
        return ads.stream().map(this::recommendAd).sorted(BEST_FIRST).toList();
    }

    public AdRecommendation recommendAd(Ad ad) {
        double chance = ad.probability().successChance();
        return new AdRecommendation(ad, chance, ad.reward() * chance, isRecommended(ad, chance));
    }

    /**
     * Picks the ad to play from a ranked board; empty when no ad on it is recommended. Plays cautiously while a
     * lost life could not be replaced: when lives are low, or when there is not enough gold for a healing potion.
     */
    public Optional<AdRecommendation> chooseAd(List<AdRecommendation> board, int lives, int gold) {
        List<AdRecommendation> recommended = board.stream().filter(AdRecommendation::recommended).toList();
        return isVulnerable(lives, gold) ? chooseCautiously(recommended) : chooseBoldly(recommended);
    }

    /** An ad is recommended when the odds are good enough, it will still be there, and it is honest work. */
    private boolean isRecommended(Ad ad, double chance) {
        return chance >= properties.recommendedMinChance()
                && ad.expiresIn() >= properties.recommendedMinTurnsLeft()
                && !REPUTATION_DAMAGING.matcher(ad.message()).find();
    }

    private boolean isVulnerable(int lives, int gold) {
        return lives <= properties.lowLives() || gold < properties.potionMinGold();
    }

    /** Plenty of lives: the most valuable recommended ad. */
    private Optional<AdRecommendation> chooseBoldly(List<AdRecommendation> recommended) {
        return recommended.stream().findFirst();
    }

    /** Few lives: the most valuable recommended ad that is also safe, else the safest recommended one. */
    private Optional<AdRecommendation> chooseCautiously(List<AdRecommendation> recommended) {
        return recommended.stream()
                .filter(candidate -> candidate.successChance() >= properties.safeChance())
                .findFirst()
                .or(() -> recommended.stream().max(BY_CHANCE));
    }
}
