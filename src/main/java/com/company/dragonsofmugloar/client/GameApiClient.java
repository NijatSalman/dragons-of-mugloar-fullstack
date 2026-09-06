package com.company.dragonsofmugloar.client;

import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.game.Reputation;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import java.util.List;

/**
 * Access to the external game server, expressed in domain terms. Services depend on this interface only;
 * implementations translate transport details into domain objects and into
 * {@link com.company.dragonsofmugloar.exception.GameNotFoundException},
 * {@link com.company.dragonsofmugloar.exception.GameOverException} or
 * {@link com.company.dragonsofmugloar.exception.GameApiException}.
 */
public interface GameApiClient {

    Game startGame(GameOrigin origin);

    List<Ad> getAds(String gameId);

    SolveResult solveAd(String gameId, String adId);

    List<ShopItem> getShopItems(String gameId);

    PurchaseResult buyItem(String gameId, String itemId);

    Reputation investigateReputation(String gameId);
}
