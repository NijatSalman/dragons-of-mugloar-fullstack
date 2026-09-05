package com.company.dragonsofmugloar.client.dto;

/** Payload exactly as the game server sends it; never leaves the client package. */
public record ShopItemPayload(String id, String name, int cost) {
}
