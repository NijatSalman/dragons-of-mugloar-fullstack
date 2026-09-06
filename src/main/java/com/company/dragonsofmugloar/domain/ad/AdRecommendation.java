package com.company.dragonsofmugloar.domain.ad;

/**
 * Our judgement of one ad: how likely it is to succeed, what it is worth on average, and whether it is a
 * sensible pick. Shown to the player as a hint and used by the autoplayer to choose.
 *
 * @param successChance probability of solving the ad, from {@link Probability}
 * @param expectedValue reward weighted by the chance of actually receiving it
 * @param recommended   true when odds, remaining time and the nature of the task all pass the recommender's rules
 */
public record AdRecommendation(Ad ad, double successChance, double expectedValue, boolean recommended) {
}
