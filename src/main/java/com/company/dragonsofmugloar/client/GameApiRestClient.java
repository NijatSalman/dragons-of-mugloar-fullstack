package com.company.dragonsofmugloar.client;

import static com.company.dragonsofmugloar.exception.GameApiException.Reason.REJECTED;
import static com.company.dragonsofmugloar.exception.GameApiException.Reason.UNAVAILABLE;

import com.company.dragonsofmugloar.client.dto.GameStartResponse;
import com.company.dragonsofmugloar.client.dto.MessageResponse;
import com.company.dragonsofmugloar.client.dto.PurchaseResponse;
import com.company.dragonsofmugloar.client.dto.ReputationResponse;
import com.company.dragonsofmugloar.client.dto.ShopItemResponse;
import com.company.dragonsofmugloar.client.dto.SolveResponse;
import com.company.dragonsofmugloar.config.GameApiProperties;
import com.company.dragonsofmugloar.domain.Ad;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.domain.PurchaseResult;
import com.company.dragonsofmugloar.domain.Reputation;
import com.company.dragonsofmugloar.domain.ShopItem;
import com.company.dragonsofmugloar.domain.SolveResult;
import com.company.dragonsofmugloar.exception.GameApiException;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.exception.GameOverException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.resilience.retry.MethodRetryPredicate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * {@link GameApiClient} implementation on Spring's {@link RestClient}. Wire DTOs are mapped to domain objects
 * here, encoded ads are decoded here, and HTTP failures become application exceptions, so nothing outside this
 * package knows how the game server speaks. Only idempotent reads are retried; a solve or a purchase is never
 * sent twice because the first attempt may already have been applied.
 */
@Component
class GameApiRestClient implements GameApiClient {

    private static final ParameterizedTypeReference<List<MessageResponse>> MESSAGE_LIST =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<ShopItemResponse>> SHOP_LIST =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    GameApiRestClient(RestClient.Builder builder, GameApiProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public Game startGame() {
        GameStartResponse r = call(null, () -> restClient.post().uri("/game/start")
                .retrieve().body(GameStartResponse.class));
        return new Game(r.gameId(), r.lives(), r.gold(), r.level(), r.score(), r.highScore(), r.turn());
    }

    @Override
    @Retryable(includes = GameApiException.class, predicate = RetryWhenUnavailable.class, maxRetries = 1, delay = 200)
    public List<Ad> getAds(String gameId) {
        List<MessageResponse> messages = call(gameId, () -> restClient.get().uri("/{gameId}/messages", gameId)
                .retrieve().body(MESSAGE_LIST));
        return messages.stream().map(GameApiRestClient::toAd).toList();
    }

    @Override
    public SolveResult solve(String gameId, String adId) {
        SolveResponse r = call(gameId, () -> restClient.post().uri("/{gameId}/solve/{adId}", gameId, adId)
                .retrieve().body(SolveResponse.class));
        return new SolveResult(r.success(), r.lives(), r.gold(), r.score(), r.highScore(), r.turn(), r.message());
    }

    @Override
    @Retryable(includes = GameApiException.class, predicate = RetryWhenUnavailable.class, maxRetries = 1, delay = 200)
    public List<ShopItem> getShop(String gameId) {
        List<ShopItemResponse> items = call(gameId, () -> restClient.get().uri("/{gameId}/shop", gameId)
                .retrieve().body(SHOP_LIST));
        return items.stream().map(i -> new ShopItem(i.id(), i.name(), i.cost())).toList();
    }

    @Override
    public PurchaseResult buy(String gameId, String itemId) {
        PurchaseResponse r = call(gameId, () -> restClient.post()
                .uri("/{gameId}/shop/buy/{itemId}", gameId, itemId)
                .retrieve().body(PurchaseResponse.class));
        return new PurchaseResult(r.shoppingSuccess(), r.gold(), r.lives(), r.level(), r.turn());
    }

    @Override
    public Reputation investigateReputation(String gameId) {
        ReputationResponse r = call(gameId, () -> restClient.post()
                .uri("/{gameId}/investigate/reputation", gameId)
                .retrieve().body(ReputationResponse.class));
        return new Reputation(r.people(), r.state(), r.underworld());
    }

    private static Ad toAd(MessageResponse m) {
        return new Ad(
                MessageDecoder.decode(m.adId(), m.encrypted()),
                MessageDecoder.decode(m.message(), m.encrypted()),
                m.reward(),
                m.expiresIn(),
                MessageDecoder.decode(m.probability(), m.encrypted()));
    }

    /**
     * Runs one request and translates every failure into an application exception.
     *
     * @param gameId the game the request belongs to, or {@code null} when starting a new game
     */
    private static <T> T call(String gameId, Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new GameApiException(UNAVAILABLE, "Game server returned an empty response");
            }
            return body;
        } catch (RestClientResponseException e) {
            throw toException(e.getStatusCode().value(), gameId);
        } catch (ResourceAccessException e) {
            throw new GameApiException(UNAVAILABLE, "Game server is unreachable: " + e.getMessage(), e);
        } catch (RestClientException e) {
            throw new GameApiException(UNAVAILABLE, "Game server returned an unreadable response: " + e.getMessage(), e);
        }
    }

    private static RuntimeException toException(int status, String gameId) {
        return switch (status) {
            case 400 -> new GameApiException(REJECTED, "Game server rejected the request for game " + gameId);
            case 404 -> new GameNotFoundException(gameId);
            case 410 -> new GameOverException(gameId);
            default -> new GameApiException(UNAVAILABLE, "Game server answered HTTP " + status + " for game " + gameId);
        };
    }

    /** Only an unavailable server is worth a second attempt; a rejected request would be rejected again. */
    static class RetryWhenUnavailable implements MethodRetryPredicate {

        @Override
        public boolean shouldRetry(Method method, Throwable throwable) {
            return throwable instanceof GameApiException e && e.reason() == UNAVAILABLE;
        }
    }
}
