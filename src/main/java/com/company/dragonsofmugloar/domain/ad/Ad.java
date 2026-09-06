package com.company.dragonsofmugloar.domain.ad;

/**
 * An ad (task) from the message board, already decoded into plain text.
 *
 * @param expiresIn number of turns until the ad disappears from the board
 */
public record Ad(String adId, String message, int reward, int expiresIn, Probability probability) {
}
