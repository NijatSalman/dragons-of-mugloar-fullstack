package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.repository.GameRepository;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** The message board of a game: read the ads with our recommendation, solve one. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final GameApiClient gameApiClient;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final AdRecommender adRecommender;

    /** The current board, ranked best first, each ad with chance, expected value and our recommendation. */
    public List<AdRecommendation> getRecommendedAds(String gameId) {
        gameService.getGame(gameId);
        List<Ad> ads = gameApiClient.getAds(gameId);
        log.debug("Ads fetched: gameId={}, count={}", gameId, ads.size());
        return adRecommender.recommendAds(ads);
    }

    /** Attempts one ad; costs a turn and, on failure, a life. */
    public SolveResult solveAd(String gameId, String adId) {
        Game game = gameService.getGame(gameId);
        SolveResult result = gameApiClient.solveAd(gameId, adId);
        Game updated = game.afterSolve(result);
        gameRepository.save(updated);
        log.info("Ad solved: gameId={}, adId={}, success={}, score={}, lives={}, gold={}",
                gameId, adId, result.success(), result.score(), result.lives(), result.gold());
        if (updated.isOver()) {
            log.info("Game over: gameId={}, score={}, turn={}", gameId, updated.score(), updated.turn());
        }
        return result;
    }
}
