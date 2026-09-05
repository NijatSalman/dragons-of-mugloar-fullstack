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
                        {"gameId":"abc123","lives":3,"gold":0,"level":0,"score":0,"highScore":42,"turn":0}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.startGame()).isEqualTo(new Game("abc123", 3, 0, 0, 0, 42, 0));
    }

    @Test
    void getAdsMapsAndDecodesEncryptedMessages() {
        server.expect(requestTo(BASE + "/abc123/messages")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"adId":"a1","message":"Help Bob","reward":21,"expiresIn":7,"encrypted":null,
                          "probability":"Quite likely"},
                         {"adId":"YTI=","message":"SGVscA==","reward":5,"expiresIn":3,"encrypted":1,
                          "probability":"UGllY2Ugb2YgY2FrZQ=="},
                         {"adId":"n3","message":"Uryc","reward":9,"expiresIn":2,"encrypted":2,
                          "probability":"Evfxl"}]
                        """, MediaType.APPLICATION_JSON));

        List<Ad> ads = client.getAds("abc123");

        assertThat(ads).containsExactly(
                new Ad("a1", "Help Bob", 21, 7, "Quite likely"),
                new Ad("a2", "Help", 5, 3, "Piece of cake"),
                new Ad("a3", "Help", 9, 2, "Risky"));
    }

    @Test
    void solvePostsToTheAdAndMapsTheOutcome() {
        server.expect(requestTo(BASE + "/abc123/solve/a1")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"success":true,"lives":3,"gold":25,"score":25,"highScore":42,"turn":2,
                         "message":"You successfully solved the mission!"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.solve("abc123", "a1"))
                .isEqualTo(new SolveResult(true, 3, 25, 25, 42, 2, "You successfully solved the mission!"));
    }

    @Test
    void getShopMapsItems() {
        server.expect(requestTo(BASE + "/abc123/shop")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"hpot","name":"Healing potion","cost":50},{"id":"cs","name":"Claw Sharpening","cost":100}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.getShop("abc123")).containsExactly(
                new ShopItem("hpot", "Healing potion", 50),
                new ShopItem("cs", "Claw Sharpening", 100));
    }

    @Test
    void buyReportsAFailedPurchaseWithoutThrowing() {
        server.expect(requestTo(BASE + "/abc123/shop/buy/hpot")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"shoppingSuccess":false,"gold":4,"lives":3,"level":0,"turn":3}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.buy("abc123", "hpot")).isEqualTo(new PurchaseResult(false, 4, 3, 0, 3));
    }

    @Test
    void investigateReputationMapsScores() {
        server.expect(requestTo(BASE + "/abc123/investigate/reputation")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"people":1.5,"state":0,"underworld":-2}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.investigateReputation("abc123")).isEqualTo(new Reputation(1.5, 0, -2));
    }

    @Test
    void badRequestWithHtmlBodyIsARejectedRequest() {
        server.expect(requestTo(BASE + "/abc123/solve/nope"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_HTML).body(HTML_ERROR));

        assertThatThrownBy(() -> client.solve("abc123", "nope"))
                .isInstanceOfSatisfying(GameApiException.class,
                        e -> assertThat(e.reason()).isEqualTo(GameApiException.Reason.REJECTED))
                .hasMessageContaining("abc123");
    }

    @Test
    void notFoundBecomesGameNotFoundException() {
        server.expect(requestTo(BASE + "/missing/messages"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(HTML_ERROR));

        assertThatThrownBy(() -> client.getAds("missing"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void goneBecomesGameOverException() {
        server.expect(requestTo(BASE + "/abc123/solve/a1")).andRespond(withStatus(HttpStatus.GONE));

        assertThatThrownBy(() -> client.solve("abc123", "a1"))
                .isInstanceOf(GameOverException.class)
                .hasMessageContaining("abc123");
    }

    @Test
    void serverErrorOnReadIsRetriedOnceThenSucceeds() {
        server.expect(times(1), requestTo(BASE + "/abc123/messages"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(times(1), requestTo(BASE + "/abc123/messages"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.getAds("abc123")).isEmpty();
        server.verify();
    }

    @Test
    void serverErrorOnReadFailsAfterTheSingleRetry() {
        server.expect(times(2), requestTo(BASE + "/abc123/shop"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.getShop("abc123"))
                .isInstanceOfSatisfying(GameApiException.class,
                        e -> assertThat(e.reason()).isEqualTo(GameApiException.Reason.UNAVAILABLE))
                .hasMessageContaining("HTTP 500");
        server.verify();
    }

    @Test
    void rejectedReadIsNotRetried() {
        server.expect(times(1), requestTo(BASE + "/abc123/shop"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.getShop("abc123")).isInstanceOf(GameApiException.class);
        server.verify();
    }

    @Test
    void solveIsNeverRetried() {
        server.expect(times(1), requestTo(BASE + "/abc123/solve/a1")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.solve("abc123", "a1")).isInstanceOf(GameApiException.class);
        server.verify();
    }

    @Test
    void timeoutIsAnUnavailableServer() {
        server.expect(times(1), requestTo(BASE + "/abc123/solve/a1"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.solve("abc123", "a1"))
                .isInstanceOfSatisfying(GameApiException.class,
                        e -> assertThat(e.reason()).isEqualTo(GameApiException.Reason.UNAVAILABLE))
                .hasMessageContaining("unreachable");
    }
}
