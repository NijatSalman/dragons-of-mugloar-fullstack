package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.observability.GameMetrics;
import com.company.dragonsofmugloar.repository.BoardRepository;
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
    private final BoardRepository boardRepository;
    private final GameService gameService;
    private final AdRecommender adRecommender;
    private final GameMetrics gameMetrics;

    /** The current board, ranked best first, each ad with chance, expected value and our recommendation. */
    public List<AdRecommendation> getRecommendedAds(String gameId) {
        gameService.getGame(gameId);
        List<Ad> ads = gameApiClient.getAds(gameId);
        boardRepository.save(gameId, ads);
        log.debug("Ads fetched: gameId={}, count={}", gameId, ads.size());
        return adRecommender.recommendAds(ads);
    }

    /** Attempts one ad; costs a turn and, on failure, a life. */
    public SolveResult solveAd(String gameId, String adId) {
        Game game = gameService.getGame(gameId);
        Probability probability = probabilityOf(gameId, adId);
        SolveResult result = gameApiClient.solveAd(gameId, adId);
        Game updated = game.afterSolve(result);
        gameRepository.save(updated);
        gameMetrics.countAdSolved(probability, result.success());
        log.info("Ad solved: gameId={}, adId={}, probability={}, success={}, score={}, lives={}, gold={}",
                gameId, adId, probability, result.success(), result.score(), result.lives(), result.gold());
        if (updated.isOver()) {
            boardRepository.delete(gameId);
            gameMetrics.recordFinalScore(updated);
            log.info("Game over: gameId={}, score={}, turn={}", gameId, updated.score(), updated.turn());
        }
        return result;
    }

    /** The label the last fetched board showed for this ad; unknown when the ad was never listed to us. */
    private Probability probabilityOf(String gameId, String adId) {
        return boardRepository.findAd(gameId, adId).map(Ad::probability).orElse(Probability.UNKNOWN);
    }
}
