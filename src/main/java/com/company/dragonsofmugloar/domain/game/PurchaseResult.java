package com.company.dragonsofmugloar.domain.game;

/** Outcome of a shop purchase. The turn advances even when the purchase fails. */
public record PurchaseResult(boolean success, int gold, int lives, int level, int turn) {
}
