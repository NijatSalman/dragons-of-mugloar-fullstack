package com.company.dragonsofmugloar.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The whole application wired together and driven through its own REST API. Only the game server is replaced,
 * by {@link GameServerStub} answering with responses recorded from the real one.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@AutoConfigureMetrics
@TestPropertySource(properties = "game-api.base-url=https://game.test/api/v2")
class GameFlowIntegrationTest {

    private static final String GAME_ID = "ggLmesXI";

    @Autowired
    private MockMvc api;

    @Autowired
    private MockRestServiceServer mockServer;

    private GameServerStub gameServer;

    @BeforeEach
    void setUp() {
        gameServer = new GameServerStub(mockServer);
    }

    @Test
    void gameFlowStartsSolvesAndBuysThroughTheApi() throws Exception {
        gameServer.startsGame("start.json");
        gameServer.listsAds(GAME_ID, "messages.json");
        gameServer.listsShop(GAME_ID, "shop.json");
        gameServer.solvesAd(GAME_ID, "DSAUBsXa", "solve-success.json");
        gameServer.sellsItem(GAME_ID, "cs", "buy-success.json");

        api.perform(post("/api/v1/games"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(jsonPath("$.gameId").value(GAME_ID))
                .andExpect(jsonPath("$.origin").value("MANUAL"));

        // ranked by expected value: the 70-gold escort first though not recommended, then the Base64 ad decoded
        api.perform(get("/api/v1/games/{gameId}/ads", GAME_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].adId").value("3haCbU60"))
                .andExpect(jsonPath("$[0].recommended").value(false))
                .andExpect(jsonPath("$[1].adId").value("DSAUBsXa"))
                .andExpect(jsonPath("$[1].message").value("Help Majid Desprez to transport a magic beer mug to steppe in Falldean"))
                .andExpect(jsonPath("$[1].probability").value("Quite likely"))
                .andExpect(jsonPath("$[1].recommended").value(true));

        api.perform(get("/api/v1/games/{gameId}/shop", GAME_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value("hpot"));

        api.perform(post("/api/v1/games/{gameId}/ads/{adId}/solve", GAME_ID, "DSAUBsXa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.gold").value(121));

        api.perform(post("/api/v1/games/{gameId}/shop/{itemId}", GAME_ID, "cs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(1));

        api.perform(get("/api/v1/games/{gameId}", GAME_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gold").value(21))
                .andExpect(jsonPath("$.score").value(121))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.turn").value(2))
                .andExpect(jsonPath("$.over").value(false));

        gameServer.verifyAllCallsHappened();
    }

    @Test
    void solveAdReturns410WhenTheGameIsOver() throws Exception {
        gameServer.startsGame("start.json");
        gameServer.reportsGameOver(GAME_ID, "mkONU4UV");

        api.perform(post("/api/v1/games")).andExpect(status().isCreated());

        api.perform(post("/api/v1/games/{gameId}/ads/{adId}/solve", GAME_ID, "mkONU4UV"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.title").value("Gone"))
                .andExpect(jsonPath("$.detail").value("Game over: gameId=" + GAME_ID))
                .andExpect(jsonPath("$.traceId").exists());
        gameServer.verifyAllCallsHappened();
    }

    @Test
    void getRecommendedAdsReturns404WhenTheServerForgotTheGame() throws Exception {
        gameServer.startsGame("start.json");
        gameServer.doesNotKnowGame(GAME_ID);

        api.perform(post("/api/v1/games")).andExpect(status().isCreated());

        api.perform(get("/api/v1/games/{gameId}/ads", GAME_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Game not found: gameId=" + GAME_ID));
        gameServer.verifyAllCallsHappened();
    }

    @Test
    void getGameReturns404WhenTheGameWasNeverStarted() throws Exception {
        api.perform(get("/api/v1/games/{gameId}", "nope1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Game not found: gameId=nope1234"));
        gameServer.verifyAllCallsHappened();
    }
}
