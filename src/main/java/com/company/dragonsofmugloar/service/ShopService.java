package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.PurchaseResult;
import com.company.dragonsofmugloar.domain.ShopItem;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.repository.GameRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Use cases of the shop: list what is for sale and buy an item for a game. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final GameApiClient gameApiClient;
    private final GameRepository gameRepository;

    public List<ShopItem> getItems(String gameId) {
        requireGame(gameId);
        return gameApiClient.getShop(gameId);
    }

    public PurchaseResult buy(String gameId, String itemId) {
        Game game = requireGame(gameId);
        PurchaseResult result = gameApiClient.buy(gameId, itemId);
        gameRepository.save(game.afterPurchase(result));
        log.info("Item purchased: gameId={}, itemId={}, success={}, gold={}, level={}, lives={}",
                gameId, itemId, result.success(), result.gold(), result.level(), result.lives());
        return result;
    }

    private Game requireGame(String gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
