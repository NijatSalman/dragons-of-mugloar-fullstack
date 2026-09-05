package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.AdRecommendation;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.Reputation;
import com.company.dragonsofmugloar.domain.SolveResult;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.repository.GameRepository;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Use cases of one game: start it, read its state, look at the board, solve an ad, investigate reputation. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameApiClient gameApiClient;
    private final GameRepository gameRepository;
    private final AdRecommender adRecommender;

    public Game startGame() {
        Game game = gameApiClient.startGame();
        gameRepository.save(game);
        log.info("Game started: gameId={}, lives={}, gold={}", game.gameId(), game.lives(), game.gold());
        return game;
    }

    public Game getGame(String gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    /** The current board, ranked best first with our recommendation attached. */
    public List<AdRecommendation> getAds(String gameId) {
        getGame(gameId);
        List<Ad> ads = gameApiClient.getAds(gameId);
        log.debug("Ads fetched: gameId={}, count={}", gameId, ads.size());
        return adRecommender.recommend(ads);
    }

    public SolveResult solve(String gameId, String adId) {
        Game game = getGame(gameId);
        SolveResult result = gameApiClient.solve(gameId, adId);
        Game updated = game.afterSolve(result);
        gameRepository.save(updated);
        log.info("Ad solved: gameId={}, adId={}, success={}, score={}, lives={}, gold={}",
                gameId, adId, result.success(), result.score(), result.lives(), result.gold());
        if (updated.isOver()) {
            log.info("Game over: gameId={}, score={}, turn={}", gameId, updated.score(), updated.turn());
        }
        return result;
    }

    public Reputation investigateReputation(String gameId) {
        getGame(gameId);
        Reputation reputation = gameApiClient.investigateReputation(gameId);
        log.debug("Reputation investigated: gameId={}, people={}, state={}, underworld={}",
                gameId, reputation.people(), reputation.state(), reputation.underworld());
        return reputation;
    }
}
