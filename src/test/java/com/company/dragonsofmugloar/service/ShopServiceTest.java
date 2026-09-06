package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.PurchaseResult;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.observability.GameMetrics;
import com.company.dragonsofmugloar.repository.GameRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    private static final String GAME_ID = "ggLmesXI";

    @Mock
    private GameApiClient gameApiClient;

    private final GameRepository gameRepository = new GameRepository();

    private ShopService service;

    @BeforeEach
    void setUp() {
        service = new ShopService(gameApiClient, gameRepository, new GameService(gameApiClient, gameRepository),
                new GameMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void getShopItemsReturnsTheCatalogueForAKnownGame() {
        gameRepository.save(new Game(GAME_ID, 3, 120, 0, 300, 300, 15));
        List<ShopItem> items = List.of(new ShopItem("hpot", "Healing potion", 50),
                new ShopItem("cs", "Claw Sharpening", 100));
        when(gameApiClient.getShopItems(GAME_ID)).thenReturn(items);

        assertThat(service.getShopItems(GAME_ID)).isEqualTo(items);
    }

    @Test
    void buyItemThrowsGameNotFoundBeforeCallingTheServer() {
        assertThatThrownBy(() -> service.buyItem("nope1234", "hpot")).isInstanceOf(GameNotFoundException.class);
        verifyNoInteractions(gameApiClient);
    }

    @Test
    void buyItemUpdatesGoldLevelAndTurnButKeepsTheScore() {
        gameRepository.save(new Game(GAME_ID, 3, 120, 0, 300, 300, 15));
        when(gameApiClient.buyItem(GAME_ID, "cs")).thenReturn(new PurchaseResult(true, 20, 3, 1, 16));

        PurchaseResult result = service.buyItem(GAME_ID, "cs");

        assertThat(result.success()).isTrue();
        assertThat(gameRepository.findById(GAME_ID)).contains(new Game(GAME_ID, 3, 20, 1, 300, 300, 16));
    }

    @Test
    void buyItemAdvancesTheTurnWhenPurchaseFails() {
        gameRepository.save(new Game(GAME_ID, 3, 4, 0, 4, 4, 2));
        when(gameApiClient.buyItem(GAME_ID, "hpot")).thenReturn(new PurchaseResult(false, 4, 3, 0, 3));

        service.buyItem(GAME_ID, "hpot");

        assertThat(gameRepository.findById(GAME_ID)).map(Game::turn).contains(3);
    }
}
