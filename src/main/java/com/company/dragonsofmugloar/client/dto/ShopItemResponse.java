package com.company.dragonsofmugloar.client.dto;

/** Wire format of the game server, not used outside the client package. */
public record ShopItemResponse(String id, String name, int cost) {
}
