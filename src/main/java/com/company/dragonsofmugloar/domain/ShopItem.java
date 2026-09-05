package com.company.dragonsofmugloar.domain;

/** An item that can be bought for gold, e.g. {@code hpot} "Healing potion". */
public record ShopItem(String itemId, String name, int cost) {
}
