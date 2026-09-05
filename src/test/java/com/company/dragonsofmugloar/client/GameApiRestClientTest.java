package com.company.dragonsofmugloar.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.company.dragonsofmugloar.config.GameApiProperties;
import com.company.dragonsofmugloar.config.ResilienceConfig;
import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.Probability;
import com.company.dragonsofmugloar.domain.PurchaseResult;
import com.company.dragonsofmugloar.domain.Reputation;
import com.company.dragonsofmugloar.domain.ShopItem;
import com.company.dragonsofmugloar.domain.SolveResult;
import com.company.dragonsofmugloar.exception.GameApiException;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.exception.GameOverException;
import java.net.SocketTimeoutException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(GameApiRestClient.class)
@EnableConfigurationProperties(GameApiProperties.class)
@Import(ResilienceConfig.class)
@TestPropertySource(properties = "game-api.base-url=https://game.test/api/v2")
class GameApiRestClientTest {

    private static final String BASE = "https://game.test/api/v2";
    private static final String HTML_ERROR = "<!DOCTYPE html><html><body><pre>Bad Request</pre></body></html>";

    @Autowired
    private GameApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void startGameMapsToGame() {
        server.expect(requestTo(BASE + "/game/start")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"gameId":"ggLmesXI","lives":3,"gold":0,"level":0,"score":0,"highScore":0,"turn":0}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.startGame()).isEqualTo(new Game("ggLmesXI", 3, 0, 0, 0, 0, 0));
    }

    @Test
    void getAdsMapsAndDecodesEncryptedMessages() {
        server.expect(requestTo(BASE + "/ggLmesXI/messages")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"adId":"DSAUBsXa","message":"Help Majid Desprez to transport a magic beer mug to steppe in Falldean",
                          "reward":21,"expiresIn":7,"encrypted":null,"probability":"Quite likely"},
                         {"adId":"SGlDdFl4SEM=","message":"SGVscCBQcmFza292aXlhIFJpY2hhcmQgdG8gZml4IHRoZWlyIGJlZXIgbXVn",
                          "reward":4,"expiresIn":7,"encrypted":1,"probability":"UGllY2Ugb2YgY2FrZQ=="},
                         {"adId":"jJKDggpW","message":"Uryc Cerrpun Fnhaqref gb pyrna gurve punevbg",
                          "reward":3,"expiresIn":7,"encrypted":2,"probability":"Evfxl"}]
                        """, MediaType.APPLICATION_JSON));

        List<Ad> ads = client.getAds("ggLmesXI");

        assertThat(ads).containsExactly(
                new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean", 21, 7,
                        Probability.QUITE_LIKELY),
                new Ad("HiCtYxHC", "Help Praskoviya Richard to fix their beer mug", 4, 7, Probability.PIECE_OF_CAKE),
                new Ad("wWXQttcJ", "Help Preecha Saunders to clean their chariot", 3, 7, Probability.RISKY));
    }

    @Test
    void solvePostsToTheAdAndMapsTheOutcome() {
        server.expect(requestTo(BASE + "/ggLmesXI/solve/DSAUBsXa")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"success":true,"lives":3,"gold":4,"score":4,"highScore":0,"turn":2,
                         "message":"You successfully solved the mission!"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.solve("ggLmesXI", "DSAUBsXa"))
                .isEqualTo(new SolveResult(true, 3, 4, 4, 0, 2, "You successfully solved the mission!"));
    }

    @Test
    void getShopMapsItems() {
        server.expect(requestTo(BASE + "/ggLmesXI/shop")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"hpot","name":"Healing potion","cost":50},{"id":"cs","name":"Claw Sharpening","cost":100}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getShop("ggLmesXI")).containsExactly(
                new ShopItem("hpot", "Healing potion", 50),
                new ShopItem("cs", "Claw Sharpening", 100));
    }

    @Test
    void buyReportsAFailedPurchaseWithoutThrowing() {
        server.expect(requestTo(BASE + "/ggLmesXI/shop/buy/hpot")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"shoppingSuccess":false,"gold":4,"lives":3,"level":0,"turn":3}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.buy("ggLmesXI", "hpot")).isEqualTo(new PurchaseResult(false, 4, 3, 0, 3));
    }

    @Test
    void investigateReputationMapsScores() {
        server.expect(requestTo(BASE + "/ggLmesXI/investigate/reputation")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"people":0.4,"state":-1.2,"underworld":0}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.investigateReputation("ggLmesXI")).isEqualTo(new Reputation(0.4, -1.2, 0));
    }

    @Test
    void badRequestWithHtmlBodyIsARejectedRequest() {
        server.expect(requestTo(BASE + "/ggLmesXI/solve/HiCtYxHC"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_HTML).body(HTML_ERROR));

        assertThatThrownBy(() -> client.solve("ggLmesXI", "HiCtYxHC"))
                .isInstanceOfSatisfying(GameApiException.class,
                        failure -> assertThat(failure.getReason()).isEqualTo(GameApiException.Reason.REJECTED))
                .hasMessageContaining("gameId=ggLmesXI");
    }

    @Test
    void notFoundBecomesGameNotFoundException() {
        server.expect(requestTo(BASE + "/nope1234/messages"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(HTML_ERROR));

        assertThatThrownBy(() -> client.getAds("nope1234"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining("gameId=nope1234");
    }

    @Test
    void goneBecomesGameOverException() {
        server.expect(requestTo(BASE + "/ggLmesXI/solve/DSAUBsXa")).andRespond(withStatus(HttpStatus.GONE));

        assertThatThrownBy(() -> client.solve("ggLmesXI", "DSAUBsXa"))
                .isInstanceOf(GameOverException.class)
                .hasMessageContaining("Game over: gameId=ggLmesXI");
    }

    @Test
    void serverErrorOnReadIsRetriedOnceThenSucceeds() {
        server.expect(times(1), requestTo(BASE + "/ggLmesXI/messages"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(times(1), requestTo(BASE + "/ggLmesXI/messages"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.getAds("ggLmesXI")).isEmpty();
        server.verify();
    }

    @Test
    void serverErrorOnReadFailsAfterTheSingleRetry() {
        server.expect(times(2), requestTo(BASE + "/ggLmesXI/shop"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.getShop("ggLmesXI"))
                .isInstanceOfSatisfying(GameApiException.class,
                        failure -> assertThat(failure.getReason()).isEqualTo(GameApiException.Reason.UNAVAILABLE))
                .hasMessageContaining("status=500");
        server.verify();
    }


    @Test
    void solveIsNeverRetried() {
        server.expect(times(1), requestTo(BASE + "/ggLmesXI/solve/DSAUBsXa")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.solve("ggLmesXI", "DSAUBsXa")).isInstanceOf(GameApiException.class);
        server.verify();
    }

    @Test
    void timeoutIsAnUnavailableServer() {
        server.expect(times(1), requestTo(BASE + "/ggLmesXI/solve/DSAUBsXa"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.solve("ggLmesXI", "DSAUBsXa"))
                .isInstanceOfSatisfying(GameApiException.class,
                        failure -> assertThat(failure.getReason()).isEqualTo(GameApiException.Reason.UNAVAILABLE))
                .hasMessageContaining("Game server unreachable: gameId=ggLmesXI");
    }
}
