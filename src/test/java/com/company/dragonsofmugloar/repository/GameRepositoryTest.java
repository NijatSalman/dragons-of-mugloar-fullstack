package com.company.dragonsofmugloar.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import org.junit.jupiter.api.Test;

class GameRepositoryTest {

    private final GameRepository repository = new GameRepository();

    @Test
    void findByIdReturnsTheSavedGame() {
        Game game = new Game("ggLmesXI", 3, 0, 0, 0, 0, 0, GameOrigin.MANUAL);

        repository.save(game);

        assertThat(repository.findById("ggLmesXI")).contains(game);
    }

    @Test
    void saveReplacesThePreviousState() {
        repository.save(new Game("ggLmesXI", 3, 0, 0, 0, 0, 0, GameOrigin.MANUAL));
        repository.save(new Game("ggLmesXI", 2, 45, 1, 210, 210, 9, GameOrigin.MANUAL));

        assertThat(repository.findById("ggLmesXI")).map(Game::score).contains(210);
    }

    @Test
    void findAllByScoreDescRanksEveryGame() {
        repository.save(new Game("ggLmesXI", 0, 87, 3, 1462, 1462, 41, GameOrigin.MANUAL));
        repository.save(new Game("jz21oOWI", 0, 12, 4, 5239, 5239, 201, GameOrigin.AUTOPLAY));
        repository.save(new Game("0NVG7E0r", 2, 40, 1, 380, 380, 12, GameOrigin.MANUAL));

        assertThat(repository.findAllByScoreDesc()).extracting(Game::gameId).containsExactly("jz21oOWI", "ggLmesXI", "0NVG7E0r");
    }

    @Test
    void findByIdReturnsEmptyWhenIdIsUnknown() {
        assertThat(repository.findById("nope1234")).isEmpty();
    }
}
