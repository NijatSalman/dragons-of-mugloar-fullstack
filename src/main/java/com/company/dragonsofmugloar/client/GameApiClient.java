package com.company.dragonsofmugloar.client;

import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.PurchaseResult;
import com.company.dragonsofmugloar.domain.Reputation;
import com.company.dragonsofmugloar.domain.ShopItem;
import com.company.dragonsofmugloar.domain.SolveResult;
import java.util.List;

/**
 * Access to the external game server, expressed in domain terms. Services depend on this interface only;
 * implementations translate transport details into domain objects and into
 * {@link com.company.dragonsofmugloar.exception.GameNotFoundException},
 * {@link com.company.dragonsofmugloar.exception.GameOverException} or
 * {@link com.company.dragonsofmugloar.exception.GameApiException}.
 */
public interface GameApiClient {

    Game startGame();

    List<Ad> getAds(String gameId);

    SolveResult solve(String gameId, String adId);

    List<ShopItem> getShop(String gameId);

    PurchaseResult buy(String gameId, String itemId);

    Reputation investigateReputation(String gameId);
}
