package com.company.dragonsofmugloar.domain;

/**
 * An ad (task) from the message board, already decoded into plain text.
 *
 * @param probability the server's textual estimate of success, e.g. "Piece of cake" or "Suicide mission"
 * @param expiresIn   number of turns until the ad disappears from the board
 */
public record Ad(String adId, String message, int reward, int expiresIn, String probability) {
}
