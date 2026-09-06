package com.company.dragonsofmugloar.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import com.company.dragonsofmugloar.exception.GameApiException;
import com.company.dragonsofmugloar.service.ShopService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ShopController.class)
class ShopControllerTest {

    private static final String SHOP_URL = "/api/v1/games/ggLmesXI/shop";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ShopService shopService;

    @Test
    void listsItems() throws Exception {
        when(shopService.getShopItems("ggLmesXI")).thenReturn(List.of(
                new ShopItem("hpot", "Healing potion", 50), new ShopItem("cs", "Claw Sharpening", 100)));

        mvc.perform(get(SHOP_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemId").value("hpot"))
                .andExpect(jsonPath("$[0].name").value("Healing potion"))
                .andExpect(jsonPath("$[0].cost").value(50));
    }

    @Test
    void buysAnItem() throws Exception {
        when(shopService.buyItem("ggLmesXI", "cs")).thenReturn(new PurchaseResult(true, 20, 3, 1, 16));

        mvc.perform(post(SHOP_URL + "/cs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.gold").value(20))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.turn").value(16));
    }

    @Test
    void unavailableGameServerIs502() throws Exception {
        when(shopService.getShopItems("ggLmesXI")).thenThrow(new GameApiException("Game server failed: gameId=ggLmesXI, status=503"));

        mvc.perform(get(SHOP_URL))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Game server is currently unavailable"));
    }

    @Test
    void malformedItemIdIs400() throws Exception {
        mvc.perform(post(SHOP_URL + "/not%20valid!")).andExpect(status().isBadRequest());
    }
}
