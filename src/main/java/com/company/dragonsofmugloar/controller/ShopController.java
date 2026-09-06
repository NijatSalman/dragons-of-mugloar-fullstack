package com.company.dragonsofmugloar.controller;

import static com.company.dragonsofmugloar.controller.GameController.ID_PATTERN;

import com.company.dragonsofmugloar.controller.dto.PurchaseResultResponse;
import com.company.dragonsofmugloar.controller.dto.ShopItemResponse;
import com.company.dragonsofmugloar.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games/{gameId}/shop")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Spend gold on potions and dragon upgrades")
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    @Operation(summary = "Items for sale")
    public List<ShopItemResponse> getItems(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return shopService.getShopItems(gameId).stream().map(ShopItemResponse::from).toList();
    }

    @PostMapping("/{itemId}")
    @Operation(summary = "Buy an item; costs one turn even if the purchase fails")
    public PurchaseResultResponse buyItem(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId,
                                @PathVariable @Pattern(regexp = ID_PATTERN) String itemId) {
        return PurchaseResultResponse.from(shopService.buyItem(gameId, itemId));
    }
}
