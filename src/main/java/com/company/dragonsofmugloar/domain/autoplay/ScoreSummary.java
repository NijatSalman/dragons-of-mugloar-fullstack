package com.company.dragonsofmugloar.domain.autoplay;

/** Lowest, average and highest score of the finished games in a session. */
public record ScoreSummary(int min, long avg, int max) {
}
