package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.TestProperties;
import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.config.AutoplayProperties;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.exception.AdNotAvailableException;
import com.company.dragonsofmugloar.observability.GameMetrics;
import com.company.dragonsofmugloar.repository.BoardRepository;
import com.company.dragonsofmugloar.repository.GameRepository;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import com.company.dragonsofmugloar.service.strategy.PurchasePolicy;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Drives the bot against a mocked game server through the real services and strategies. */
@ExtendWith(MockitoExtension.class)
class GamePlayerTest {

    private static final String GAME_ID = "0NVG7E0r";
    private static final Ad SAFE_AD = new Ad("KdX1gKEs", "Help Funda Cropper to sell an unordinary house on the local market",
            34, 7, Probability.PIECE_OF_CAKE);

    @Mock
    private GameApiClient gameApiClient;

    private GamePlayer player;

    @BeforeEach
    void setUp() {
        AutoplayProperties properties = TestProperties.autoplay();
        GameRepository games = new GameRepository();
        GameMetrics metrics = new GameMetrics(new SimpleMeterRegistry());
        GameService gameService = new GameService(gameApiClient, games);
        AdRecommender recommender = new AdRecommender(properties);
        player = new GamePlayer(gameService,
                new AdService(gameApiClient, games, new BoardRepository(), gameService, recommender, metrics),
                new ShopService(gameApiClient, games, gameService, metrics), recommender,
                new PurchasePolicy(properties), properties);
        when(gameApiClient.getAds(GAME_ID)).thenReturn(List.of(SAFE_AD));
    }

    @Test
    void playNewGameSolvesTurnAfterTurnUntilNoLivesRemain() {
        when(gameApiClient.startGame()).thenReturn(new Game(GAME_ID, 3, 0, 0, 0, 0, 0));
        when(gameApiClient.solveAd(GAME_ID, "KdX1gKEs")).thenReturn(
                new SolveResult(false, 2, 0, 0, 0, 1, "You failed on the mission!"),
                new SolveResult(false, 1, 0, 0, 0, 2, "You failed on the mission!"),
                new SolveResult(false, 0, 0, 0, 0, 3, "You failed on the mission!"));
        List<Game> progress = new ArrayList<>();

        Game finished = player.playNewGame(progress::add);

        assertThat(finished.isOver()).isTrue();
        assertThat(progress).extracting(Game::lives).containsExactly(2, 1, 0);
        verify(gameApiClient, times(3)).solveAd(GAME_ID, "KdX1gKEs");
        verify(gameApiClient, never()).buyItem(anyString(), anyString());
    }

    @Test
    void playNewGameBuysWhatThePolicySaysAfterSolving() {
        when(gameApiClient.startGame()).thenReturn(new Game(GAME_ID, 3, 120, 0, 300, 300, 10));
        when(gameApiClient.solveAd(GAME_ID, "KdX1gKEs")).thenReturn(
                new SolveResult(true, 3, 154, 334, 334, 11, "You successfully solved the mission!"),
                new SolveResult(false, 0, 54, 334, 334, 13, "You failed on the mission!"));
        when(gameApiClient.buyItem(GAME_ID, "cs")).thenReturn(new PurchaseResult(true, 54, 3, 1, 12));

        player.playNewGame(game -> { });

        verify(gameApiClient).buyItem(GAME_ID, "cs");
    }

    @Test
    void playNewGameSkipsAnAdThatVanishedBeforeSolving() {
        when(gameApiClient.startGame()).thenReturn(new Game(GAME_ID, 3, 0, 0, 0, 0, 0));
        when(gameApiClient.solveAd(GAME_ID, "KdX1gKEs"))
                .thenThrow(new AdNotAvailableException(GAME_ID, "KdX1gKEs"))
                .thenReturn(new SolveResult(false, 0, 0, 0, 0, 2, "You failed on the mission!"));

        Game finished = player.playNewGame(game -> { });

        assertThat(finished.isOver()).isTrue();
        verify(gameApiClient, times(2)).solveAd(GAME_ID, "KdX1gKEs");
    }
}
