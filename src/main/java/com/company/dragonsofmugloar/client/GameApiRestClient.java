package com.company.dragonsofmugloar.client;

import static com.company.dragonsofmugloar.exception.GameApiException.Reason.REJECTED;
import static com.company.dragonsofmugloar.exception.GameApiException.Reason.UNAVAILABLE;

import com.company.dragonsofmugloar.client.dto.StartGamePayload;
import com.company.dragonsofmugloar.client.dto.MessagePayload;
import com.company.dragonsofmugloar.client.dto.BuyPayload;
import com.company.dragonsofmugloar.client.dto.ReputationPayload;
import com.company.dragonsofmugloar.client.dto.ShopItemPayload;
import com.company.dragonsofmugloar.client.dto.SolvePayload;
import com.company.dragonsofmugloar.config.GameApiProperties;
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
import java.util.List;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
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

    private static final ParameterizedTypeReference<List<MessagePayload>> MESSAGE_LIST =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<ShopItemPayload>> SHOP_LIST =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    GameApiRestClient(RestClient.Builder builder, GameApiProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public Game startGame() {
        StartGamePayload response = call(null, () -> restClient.post().uri("/game/start")
                .retrieve().body(StartGamePayload.class));
        return new Game(response.gameId(), response.lives(), response.gold(), response.level(), response.score(),
                response.highScore(), response.turn());
    }

    @Override
    @Retryable(includes = GameApiException.class, maxRetries = 1, delay = 200)
    public List<Ad> getAds(String gameId) {
        List<MessagePayload> messages = call(gameId, () -> restClient.get().uri("/{gameId}/messages", gameId)
                .retrieve().body(MESSAGE_LIST));
        return messages.stream().map(GameApiRestClient::toAd).toList();
    }

    @Override
    public SolveResult solve(String gameId, String adId) {
        SolvePayload response = call(gameId, () -> restClient.post().uri("/{gameId}/solve/{adId}", gameId, adId)
                .retrieve().body(SolvePayload.class));
        return new SolveResult(response.success(), response.lives(), response.gold(), response.score(),
                response.highScore(), response.turn(), response.message());
    }

    @Override
    @Retryable(includes = GameApiException.class, maxRetries = 1, delay = 200)
    public List<ShopItem> getShop(String gameId) {
        List<ShopItemPayload> items = call(gameId, () -> restClient.get().uri("/{gameId}/shop", gameId)
                .retrieve().body(SHOP_LIST));
        return items.stream().map(item -> new ShopItem(item.id(), item.name(), item.cost())).toList();
    }

    @Override
    public PurchaseResult buy(String gameId, String itemId) {
        BuyPayload response = call(gameId, () -> restClient.post()
                .uri("/{gameId}/shop/buy/{itemId}", gameId, itemId)
                .retrieve().body(BuyPayload.class));
        return new PurchaseResult(response.shoppingSuccess(), response.gold(), response.lives(), response.level(),
                response.turn());
    }

    @Override
    public Reputation investigateReputation(String gameId) {
        ReputationPayload response = call(gameId, () -> restClient.post()
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
    private static <T> T call(String gameId, Supplier<T> request) {
        try {
            T body = request.get();
            if (body == null) {
                throw new GameApiException(UNAVAILABLE, "Game server response empty: gameId=" + gameId);
            }
            return body;
        } catch (RestClientResponseException httpError) {
            throw toException(httpError.getStatusCode().value(), gameId);
        } catch (ResourceAccessException networkError) {
            throw new GameApiException(UNAVAILABLE,
                    "Game server unreachable: gameId=" + gameId + ", cause=" + networkError.getMessage(), networkError);
        } catch (RestClientException clientError) {
            throw new GameApiException(UNAVAILABLE,
                    "Game server response unreadable: gameId=" + gameId + ", cause=" + clientError.getMessage(), clientError);
        }
    }

    private static RuntimeException toException(int status, String gameId) {
        return switch (status) {
            case 400 -> new GameApiException(REJECTED, "Game server rejected request: gameId=" + gameId);
            case 404 -> new GameNotFoundException(gameId);
            case 410 -> new GameOverException(gameId);
            default -> new GameApiException(UNAVAILABLE, "Game server failed: gameId=" + gameId + ", status=" + status);
        };
    }
}
