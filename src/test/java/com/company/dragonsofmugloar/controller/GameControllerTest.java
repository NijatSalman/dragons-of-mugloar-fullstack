package com.company.dragonsofmugloar.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.AdRecommendation;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.Reputation;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.exception.AdNotAvailableException;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.exception.GameOverException;
import com.company.dragonsofmugloar.service.AdService;
import com.company.dragonsofmugloar.service.GameService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameController.class)
class GameControllerTest {

    private static final String GAMES_URL = "/api/v1/games";
    private static final String GAME_ID = "ggLmesXI";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private AdService adService;

    @Test
    void startGameReturns201WithTheNewGame() throws Exception {
        when(gameService.startGame(GameOrigin.MANUAL)).thenReturn(new Game(GAME_ID, 3, 0, 0, 0, 0, 0, GameOrigin.MANUAL));

        mvc.perform(post(GAMES_URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(GAME_ID))
                .andExpect(jsonPath("$.lives").value(3))
                .andExpect(jsonPath("$.over").value(false));
    }

    @Test
    void listGamesReturnsTheLeaderboardHighestScoreFirst() throws Exception {
        when(gameService.listGamesByScore()).thenReturn(List.of(
                new Game("jz21oOWI", 0, 12, 4, 5239, 5239, 201, GameOrigin.AUTOPLAY),
                new Game(GAME_ID, 2, 120, 1, 300, 300, 15, GameOrigin.MANUAL)));

        mvc.perform(get(GAMES_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].gameId").value("jz21oOWI"))
                .andExpect(jsonPath("$[0].origin").value("AUTOPLAY"))
                .andExpect(jsonPath("$[0].score").value(5239))
                .andExpect(jsonPath("$[0].gold").value(12))
                .andExpect(jsonPath("$[0].level").value(4))
                .andExpect(jsonPath("$[0].over").value(true))
                .andExpect(jsonPath("$[1].origin").value("MANUAL"));
    }

    @Test
    void getGameReturnsTheStoredState() throws Exception {
        when(gameService.getGame(GAME_ID)).thenReturn(new Game(GAME_ID, 0, 87, 3, 1462, 1462, 41, GameOrigin.MANUAL));

        mvc.perform(get(GAMES_URL + "/" + GAME_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(1462))
                .andExpect(jsonPath("$.over").value(true));
    }

    @Test
    void getGameReturns404WhenGameIsUnknown() throws Exception {
        when(gameService.getGame("nope1234")).thenThrow(new GameNotFoundException("nope1234"));

        mvc.perform(get(GAMES_URL + "/nope1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Game not found: gameId=nope1234"));
    }

    @Test
    void getGameReturns400WhenIdIsMalformed() throws Exception {
        mvc.perform(get(GAMES_URL + "/not%20valid!"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(gameService);
    }

    @Test
    void getRecommendedAdsReturnsRankedAdsWithRecommendation() throws Exception {
        Ad ad = new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean", 21, 7,
                Probability.QUITE_LIKELY);
        when(adService.getRecommendedAds(GAME_ID)).thenReturn(List.of(new AdRecommendation(ad, 0.8, 16.8, true)));

        mvc.perform(get(GAMES_URL + "/" + GAME_ID + "/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].adId").value("DSAUBsXa"))
                .andExpect(jsonPath("$[0].probability").value("Quite likely"))
                .andExpect(jsonPath("$[0].successChance").value(0.8))
                .andExpect(jsonPath("$[0].expectedValue").value(16.8))
                .andExpect(jsonPath("$[0].recommended").value(true));
    }

    @Test
    void getRecommendedAdsReturns410WhenGameIsOver() throws Exception {
        when(adService.getRecommendedAds(GAME_ID)).thenThrow(new GameOverException(GAME_ID));

        mvc.perform(get(GAMES_URL + "/" + GAME_ID + "/ads")).andExpect(status().isGone());
    }

    @Test
    void solveAdReturnsTheOutcome() throws Exception {
        when(adService.solveAd(GAME_ID, "DSAUBsXa"))
                .thenReturn(new SolveResult(true, 3, 21, 21, 0, 2, "You successfully solved the mission!"));

        mvc.perform(post(GAMES_URL + "/" + GAME_ID + "/ads/DSAUBsXa/solve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.gold").value(21))
                .andExpect(jsonPath("$.message").value("You successfully solved the mission!"));
    }

    @Test
    void solveAdReturns409WhenAdIsGone() throws Exception {
        when(adService.solveAd(GAME_ID, "HiCtYxHC")).thenThrow(new AdNotAvailableException(GAME_ID, "HiCtYxHC"));

        mvc.perform(post(GAMES_URL + "/" + GAME_ID + "/ads/HiCtYxHC/solve")).andExpect(status().isConflict());
    }

    @Test
    void investigateReputationReturnsTheThreeScores() throws Exception {
        when(gameService.investigateReputation(GAME_ID)).thenReturn(new Reputation(0.4, -1.2, 0));

        mvc.perform(post(GAMES_URL + "/" + GAME_ID + "/reputation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.people").value(0.4))
                .andExpect(jsonPath("$.state").value(-1.2))
                .andExpect(jsonPath("$.underworld").value(0));
    }
}
