package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.shop.ShopItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An item for sale")
public record ShopItemResponse(
        @Schema(example = "hpot") String itemId,
        @Schema(example = "Healing potion") String name,
        @Schema(example = "50") int cost) {

    public static ShopItemResponse from(ShopItem item) {
        return new ShopItemResponse(item.itemId(), item.name(), item.cost());
    }
}
