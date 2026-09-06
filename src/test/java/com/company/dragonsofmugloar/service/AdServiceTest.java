package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.TestProperties;
import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.observability.GameMetrics;
import com.company.dragonsofmugloar.repository.BoardRepository;
import com.company.dragonsofmugloar.repository.GameRepository;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    private static final String GAME_ID = "ggLmesXI";
    private static final Game NEW_GAME = new Game(GAME_ID, 3, 0, 0, 0, 0, 0, GameOrigin.MANUAL);

    @Mock
    private GameApiClient gameApiClient;

    private final GameRepository gameRepository = new GameRepository();
    private final BoardRepository boardRepository = new BoardRepository();

    private AdService service;

    @BeforeEach
    void setUp() {
        service = new AdService(gameApiClient, gameRepository, boardRepository,
                new GameService(gameApiClient, gameRepository), new AdRecommender(TestProperties.autoplay()),
                new GameMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void getRecommendedAdsThrowsGameNotFoundBeforeCallingTheServer() {
        assertThatThrownBy(() -> service.getRecommendedAds("nope1234")).isInstanceOf(GameNotFoundException.class);
        verifyNoInteractions(gameApiClient);
    }

    @Test
    void getRecommendedAdsReturnsRankedAdsWithRecommendation() {
        gameRepository.save(NEW_GAME);
        when(gameApiClient.getAds(GAME_ID)).thenReturn(List.of(
                new Ad("HiCtYxHC", "Help Praskoviya Richard to fix their beer mug", 4, 7, Probability.PIECE_OF_CAKE),
                new Ad("3haCbU60", "Escort Gervase Wheeler to grassland in Frostdinny where they can meet with their "
                        + "long lost chicken", 70, 7, Probability.PLAYING_WITH_FIRE),
                new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean", 21, 7,
                        Probability.QUITE_LIKELY)));

        List<AdRecommendation> ads = service.getRecommendedAds(GAME_ID);

        assertThat(ads).extracting(recommendation -> recommendation.ad().adId())
                .containsExactly("3haCbU60", "DSAUBsXa", "HiCtYxHC");
        assertThat(ads).extracting(AdRecommendation::recommended).containsExactly(false, true, true);
    }

    @Test
    void solveAdUpdatesTheStoredGame() {
        gameRepository.save(NEW_GAME);
        SolveResult result = new SolveResult(true, 3, 21, 21, 0, 2, "You successfully solved the mission!");
        when(gameApiClient.solveAd(GAME_ID, "DSAUBsXa")).thenReturn(result);

        assertThat(service.solveAd(GAME_ID, "DSAUBsXa")).isEqualTo(result);
        assertThat(gameRepository.findById(GAME_ID)).contains(new Game(GAME_ID, 3, 21, 0, 21, 0, 2, GameOrigin.MANUAL));
    }

    @Test
    void solveAdMarksTheGameOverWhenLastLifeIsLost() {
        gameRepository.save(new Game(GAME_ID, 1, 116, 0, 116, 116, 6, GameOrigin.MANUAL));
        when(gameApiClient.solveAd(GAME_ID, "mkONU4UV"))
                .thenReturn(new SolveResult(false, 0, 116, 116, 116, 7, "You failed to solve the mission."));

        service.solveAd(GAME_ID, "mkONU4UV");

        assertThat(gameRepository.findById(GAME_ID)).map(Game::isOver).contains(true);
    }
}
