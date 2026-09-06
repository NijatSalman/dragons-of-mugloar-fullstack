package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.config.AutoplayProperties;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.exception.AdNotAvailableException;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import com.company.dragonsofmugloar.service.strategy.PurchasePolicy;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The bot. Plays one game from start to game over using the same services a human uses through the UI.
 * Every turn: solve the best ad on the board, then buy what the purchase policy says.
 *
 * <p>Turns within a game session one after another, because each server call advances that game. Different games
 * are independent and session in parallel, one thread each; see {@link AutoplayGameService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GamePlayer {

    private final GameService gameService;
    private final AdService adService;
    private final ShopService shopService;
    private final AdRecommender adRecommender;
    private final PurchasePolicy purchasePolicy;
    private final AutoplayProperties properties;

    /**
     * Starts a new game and plays it until the lives run out, as the task asks, passing the game state to
     * {@code onProgress} after each turn. The turn cap only guards against a server that never ends the game.
     */
    public Game playNewGame(Consumer<Game> onProgress) {
        Game game = gameService.startGame(GameOrigin.AUTOPLAY);
        String gameId = game.gameId();
        for (int turn = 1; turn <= properties.maxTurns() && !game.isOver(); turn++) {
            AdRecommendation chosen = chooseBestAd(gameId)
                    .orElseThrow(() -> new IllegalStateException("Board empty: gameId=" + gameId));
            game = playOneTurn(gameId, chosen);
            onProgress.accept(game);
        }
        log.info("Autoplay finished: gameId={}, score={}, turn={}, lives={}",
                game.gameId(), game.score(), game.turn(), game.lives());
        return game;
    }

    /** Solve the chosen ad, buy what the policy says, return the new state. */
    private Game playOneTurn(String gameId, AdRecommendation chosen) {
        solveChosenAd(gameId, chosen);
        buyIfWorthIt(gameId);
        return gameService.getGame(gameId);
    }

    private Optional<AdRecommendation> chooseBestAd(String gameId) {
        Game game = gameService.getGame(gameId);
        List<AdRecommendation> board = adService.getRecommendedAds(gameId);
        return adRecommender.chooseAd(board, game.lives(), game.gold());
    }

    private void solveChosenAd(String gameId, AdRecommendation chosen) {
        try {
            adService.solveAd(gameId, chosen.ad().adId());
        } catch (AdNotAvailableException vanished) {
            log.info("Ad vanished before solving: gameId={}, adId={}", gameId, chosen.ad().adId());
        }
    }

    private void buyIfWorthIt(String gameId) {
        chooseItemToBuy(gameId).ifPresent(itemId -> buyChosenItem(gameId, itemId));
    }

    private void buyChosenItem(String gameId, String itemId) {
        shopService.buyItem(gameId, itemId);
    }

    private Optional<String> chooseItemToBuy(String gameId) {
        Game game = gameService.getGame(gameId);
        return purchasePolicy.chooseItemToBuy(game);
    }
}
