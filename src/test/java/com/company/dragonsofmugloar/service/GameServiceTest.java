package com.company.dragonsofmugloar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.Reputation;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.repository.GameRepository;
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
        service = new GameService(gameApiClient, gameRepository);
    }

    @Test
    void startGameStoresTheNewGame() {
        when(gameApiClient.startGame()).thenReturn(NEW_GAME);

        Game game = service.startGame();

        assertThat(game).isEqualTo(NEW_GAME);
        assertThat(gameRepository.findById(GAME_ID)).contains(NEW_GAME);
    }

    @Test
    void investigateReputationThrowsGameNotFoundBeforeCallingTheServer() {
        assertThatThrownBy(() -> service.investigateReputation("nope1234")).isInstanceOf(GameNotFoundException.class);
        verifyNoInteractions(gameApiClient);
    }

    @Test
    void investigateReputationReturnsTheServersScores() {
        gameRepository.save(NEW_GAME);
        Reputation reputation = new Reputation(0.4, -1.2, 0);
        when(gameApiClient.investigateReputation(GAME_ID)).thenReturn(reputation);

        assertThat(service.investigateReputation(GAME_ID)).isEqualTo(reputation);
    }
}
