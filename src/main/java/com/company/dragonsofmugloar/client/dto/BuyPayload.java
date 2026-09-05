package com.company.dragonsofmugloar.client.dto;

/** Payload exactly as the game server sends it; never leaves the client package. */
public record BuyPayload(boolean shoppingSuccess, int gold, int lives, int level, int turn) {
}
