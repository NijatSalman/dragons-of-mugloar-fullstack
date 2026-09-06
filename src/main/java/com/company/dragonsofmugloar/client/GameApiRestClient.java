package com.company.dragonsofmugloar.client;

import com.company.dragonsofmugloar.client.dto.StartGamePayload;
import com.company.dragonsofmugloar.client.dto.MessagePayload;
import com.company.dragonsofmugloar.client.dto.BuyPayload;
import com.company.dragonsofmugloar.client.dto.ReputationPayload;
import com.company.dragonsofmugloar.client.dto.ShopItemPayload;
import com.company.dragonsofmugloar.client.dto.SolvePayload;
import com.company.dragonsofmugloar.config.CacheConfig;
import com.company.dragonsofmugloar.config.GameApiProperties;
import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.ad.Probability;
import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.game.Reputation;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import com.company.dragonsofmugloar.domain.game.SolveResult;
import com.company.dragonsofmugloar.exception.AdNotAvailableException;
import com.company.dragonsofmugloar.exception.GameApiException;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.exception.GameOverException;
import com.company.dragonsofmugloar.exception.GameServerQuotaExceededException;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.cache.annotation.Cacheable;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * {@link GameApiClient} implementation on Spring's {@link RestClient}. Wire DTOs are mapped to domain objects
 * here, encoded ads are decoded here, and HTTP failures become application exceptions, so nothing outside this
 * package knows how the game server speaks. Every call takes a permit from the shared rate limiter, so all
 * games together stay under the server's quota. Reads are retried on any server failure; writes only when the
 * server refused the request for quota reasons, because a refused request was never applied. See
 * {@code resilience4j.*} in application.yaml.
 */
@Component
@RateLimiter(name = "gameApi")
class GameApiRestClient implements GameApiClient {

    private static final ParameterizedTypeReference<List<MessagePayload>> MESSAGE_LIST =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<ShopItemPayload>> SHOP_LIST =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    GameApiRestClient(RestClient.Builder builder, GameApiProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    @Retry(name = "gameApiWrite")
    public Game startGame(GameOrigin origin) {
        StartGamePayload response = sendRequest(null, () -> restClient.post().uri("/game/start")
                .retrieve().body(StartGamePayload.class));
        return new Game(response.gameId(), response.lives(), response.gold(), response.level(), response.score(),
                response.highScore(), response.turn(), origin);
    }

    @Override
    @Retry(name = "gameApiRead")
    public List<Ad> getAds(String gameId) {
        List<MessagePayload> messages = sendRequest(gameId, () -> restClient.get().uri("/{gameId}/messages", gameId)
                .retrieve().body(MESSAGE_LIST));
        return messages.stream().map(GameApiRestClient::toAd).toList();
    }

    @Override
    @Retry(name = "gameApiWrite")
    public SolveResult solveAd(String gameId, String adId) {
        SolvePayload response = sendRequest(gameId, () -> restClient.post().uri("/{gameId}/solve/{adId}", gameId, adId)
                .retrieve()
                .onStatus(GameApiRestClient::isBadRequest, adNotAvailable(gameId, adId))
                .body(SolvePayload.class));
        return new SolveResult(response.success(), response.lives(), response.gold(), response.score(),
                response.highScore(), response.turn(), response.message());
    }

    /**
     * The catalogue is the same for every game and does not change while the application runs, so it is fetched
     * once and served from the cache afterwards.
     */
    @Override
    @Cacheable(cacheNames = CacheConfig.SHOP_ITEMS, key = "'catalogue'")
    @Retry(name = "gameApiRead")
    public List<ShopItem> getShopItems(String gameId) {
        List<ShopItemPayload> items = sendRequest(gameId, () -> restClient.get().uri("/{gameId}/shop", gameId)
                .retrieve().body(SHOP_LIST));
        return items.stream().map(item -> new ShopItem(item.id(), item.name(), item.cost())).toList();
    }

    @Override
    @Retry(name = "gameApiWrite")
    public PurchaseResult buyItem(String gameId, String itemId) {
        BuyPayload response = sendRequest(gameId, () -> restClient.post()
                .uri("/{gameId}/shop/buy/{itemId}", gameId, itemId)
                .retrieve().body(BuyPayload.class));
        return new PurchaseResult(response.shoppingSuccess(), response.gold(), response.lives(), response.level(),
                response.turn());
    }

    @Override
    @Retry(name = "gameApiWrite")
    public Reputation investigateReputation(String gameId) {
        ReputationPayload response = sendRequest(gameId, () -> restClient.post()
                .uri("/{gameId}/investigate/reputation", gameId)
                .retrieve().body(ReputationPayload.class));
        return new Reputation(response.people(), response.state(), response.underworld());
    }

    private static Ad toAd(MessagePayload message) {
        return new Ad(
                MessageDecoder.decode(message.adId(), message.encrypted()),
                MessageDecoder.decode(message.message(), message.encrypted()),
                message.reward(),
                message.expiresIn(),
                Probability.fromLabel(MessageDecoder.decode(message.probability(), message.encrypted())));
    }

    /**
     * Runs one request and translates every failure into an application exception.
     *
     * @param gameId the game the request belongs to, or {@code null} when starting a new game
     */
    private static boolean isBadRequest(HttpStatusCode status) {
        return status.value() == HttpStatus.BAD_REQUEST.value();
    }

    /** On a solve, a rejected request means the ad is no longer on the board. */
    private static RestClient.ResponseSpec.ErrorHandler adNotAvailable(String gameId, String adId) {
        return (request, response) -> {
            throw new AdNotAvailableException(gameId, adId);
        };
    }

    private static <T> T sendRequest(String gameId, Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new GameApiException("Game server response empty: gameId=" + gameId);
            }
            return body;
        } catch (RestClientResponseException httpError) {
            throw exceptionFor(httpError.getStatusCode().value(), gameId);
        } catch (ResourceAccessException networkError) {
            throw new GameApiException("Game server unreachable: gameId=" + gameId + ", cause=" + networkError.getMessage(),
                    networkError);
        } catch (RestClientException clientError) {
            throw new GameApiException("Game server response unreadable: gameId=" + gameId + ", cause=" + clientError.getMessage(),
                    clientError);
        }
    }

    private static RuntimeException exceptionFor(int status, String gameId) {
        return switch (status) {
            case 404 -> new GameNotFoundException(gameId);
            case 410 -> new GameOverException(gameId);
            case 429 -> new GameServerQuotaExceededException(gameId);
            default -> new GameApiException("Game server failed: gameId=" + gameId + ", status=" + status);
        };
    }

}
