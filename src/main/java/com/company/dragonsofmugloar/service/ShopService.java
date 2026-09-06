package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import com.company.dragonsofmugloar.repository.GameRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** The shop of a game: list what is for sale, buy an item. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final GameApiClient gameApiClient;
    private final GameRepository gameRepository;
    private final GameService gameService;

    public List<ShopItem> getShopItems(String gameId) {
        gameService.getGame(gameId);
        return gameApiClient.getShopItems(gameId);
    }

    /** Costs one turn even when the purchase fails. */
    public PurchaseResult buyItem(String gameId, String itemId) {
        Game game = gameService.getGame(gameId);
        PurchaseResult result = gameApiClient.buyItem(gameId, itemId);
        gameRepository.save(game.afterPurchase(result));
        log.info("Item purchased: gameId={}, itemId={}, success={}, gold={}, level={}, lives={}",
                gameId, itemId, result.success(), result.gold(), result.level(), result.lives());
        return result;
    }
}
