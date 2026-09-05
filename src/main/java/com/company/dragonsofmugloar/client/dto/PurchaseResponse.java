package com.company.dragonsofmugloar.client.dto;

/** Wire format of the game server, not used outside the client package. */
public record PurchaseResponse(boolean shoppingSuccess, int gold, int lives, int level, int turn) {
}
