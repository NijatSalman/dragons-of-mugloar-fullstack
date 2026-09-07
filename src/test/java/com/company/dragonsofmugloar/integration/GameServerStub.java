package com.company.dragonsofmugloar.integration;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.ResponseCreator;

/**
 * Stands in for dragonsofmugloar.com. Every answer is a file under {@code src/test/resources/gameserver} holding
 * a response the real server sent on 2026-09-05, so the tests speak the server's actual dialect.
 */
final class GameServerStub {

    private static final String BASE_URL = "https://game.test/api/v2";

    private final MockRestServiceServer server;

    GameServerStub(MockRestServiceServer server) {
        this.server = server;
    }

    void startsGame(String responseFile) {
        expect(HttpMethod.POST, "/game/start").andRespond(json(responseFile));
    }

    void listsAds(String gameId, String responseFile) {
        expect(HttpMethod.GET, "/" + gameId + "/messages").andRespond(json(responseFile));
    }

    void listsShop(String gameId, String responseFile) {
        expect(HttpMethod.GET, "/" + gameId + "/shop").andRespond(json(responseFile));
    }

    void solvesAd(String gameId, String adId, String responseFile) {
        expect(HttpMethod.POST, "/" + gameId + "/solve/" + adId).andRespond(json(responseFile));
    }

    void sellsItem(String gameId, String itemId, String responseFile) {
        expect(HttpMethod.POST, "/" + gameId + "/shop/buy/" + itemId).andRespond(json(responseFile));
    }

    void reportsGameOver(String gameId, String adId) {
        expect(HttpMethod.POST, "/" + gameId + "/solve/" + adId)
                .andRespond(withStatus(HttpStatus.GONE).contentType(MediaType.APPLICATION_JSON).body(read("game-over.json")));
    }

    void doesNotKnowGame(String gameId) {
        expect(HttpMethod.GET, "/" + gameId + "/messages")
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(read("not-found.html")));
    }

    /** Fails the test if any expected call was not made, or an unexpected one was. */
    void verifyAllCallsHappened() {
        server.verify();
    }

    private ResponseActions expect(HttpMethod httpMethod, String path) {
        return server.expect(once(), requestTo(BASE_URL + path)).andExpect(method(httpMethod));
    }

    private static ResponseCreator json(String responseFile) {
        return withSuccess(read(responseFile), MediaType.APPLICATION_JSON);
    }

    private static String read(String responseFile) {
        try {
            return new ClassPathResource("gameserver/" + responseFile).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Missing test resource: gameserver/" + responseFile, failure);
        }
    }
}
