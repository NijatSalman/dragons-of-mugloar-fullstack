package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.AdRecommendation;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.Probability;
import com.company.dragonsofmugloar.domain.Reputation;
import com.company.dragonsofmugloar.domain.SolveResult;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.repository.GameRepository;
import com.company.dragonsofmugloar.service.strategy.AdRecommender;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final String GAME_ID = "ggLmesXI";
    private static final Game NEW_GAME = new Game(GAME_ID, 3, 0, 0, 0, 0, 0);

    @Mock
    private GameApiClient gameApiClient;

    private final GameRepository gameRepository = new GameRepository();

    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(gameApiClient, gameRepository, new AdRecommender());
    }

    @Test
    void startingAGameStoresIt() {
        when(gameApiClient.startGame()).thenReturn(NEW_GAME);

        Game game = service.startGame();

        assertThat(game).isEqualTo(NEW_GAME);
        assertThat(gameRepository.findById(GAME_ID)).contains(NEW_GAME);
    }

    @Test
    void unknownGameIsRejectedBeforeCallingTheServer() {
        assertThatThrownBy(() -> service.getAds("nope1234")).isInstanceOf(GameNotFoundException.class);
        verifyNoInteractions(gameApiClient);
    }

    @Test
    void adsComeBackRankedWithRecommendations() {
        gameRepository.save(NEW_GAME);
        when(gameApiClient.getAds(GAME_ID)).thenReturn(List.of(
                new Ad("HiCtYxHC", "Help Praskoviya Richard to fix their beer mug", 4, 7, Probability.PIECE_OF_CAKE),
                new Ad("3haCbU60", "Escort Gervase Wheeler to grassland in Frostdinny where they can meet with their "
                        + "long lost chicken", 70, 7, Probability.PLAYING_WITH_FIRE),
                new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean", 21, 7,
                        Probability.QUITE_LIKELY)));

        List<AdRecommendation> ads = service.getAds(GAME_ID);

        assertThat(ads).extracting(recommendation -> recommendation.ad().adId())
                .containsExactly("3haCbU60", "DSAUBsXa", "HiCtYxHC");
        assertThat(ads).extracting(AdRecommendation::recommended).containsExactly(false, true, true);
    }

    @Test
    void solvingUpdatesTheStoredGame() {
        gameRepository.save(NEW_GAME);
        SolveResult result = new SolveResult(true, 3, 21, 21, 0, 2, "You successfully solved the mission!");
        when(gameApiClient.solve(GAME_ID, "DSAUBsXa")).thenReturn(result);

        assertThat(service.solve(GAME_ID, "DSAUBsXa")).isEqualTo(result);
        assertThat(gameRepository.findById(GAME_ID)).contains(new Game(GAME_ID, 3, 21, 0, 21, 0, 2));
    }

    @Test
    void losingTheLastLifeMarksTheGameOver() {
        gameRepository.save(new Game(GAME_ID, 1, 116, 0, 116, 116, 6));
        when(gameApiClient.solve(GAME_ID, "mkONU4UV"))
                .thenReturn(new SolveResult(false, 0, 116, 116, 116, 7, "You failed to solve the mission."));

        service.solve(GAME_ID, "mkONU4UV");

        assertThat(gameRepository.findById(GAME_ID)).map(Game::isOver).contains(true);
    }

    @Test
    void reputationIsPassedThrough() {
        gameRepository.save(NEW_GAME);
        Reputation reputation = new Reputation(0.4, -1.2, 0);
        when(gameApiClient.investigateReputation(GAME_ID)).thenReturn(reputation);

        assertThat(service.investigateReputation(GAME_ID)).isEqualTo(reputation);
        verify(gameApiClient).investigateReputation(GAME_ID);
    }
}
