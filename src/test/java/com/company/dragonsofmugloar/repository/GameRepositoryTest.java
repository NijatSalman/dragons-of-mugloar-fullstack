package com.company.dragonsofmugloar.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.domain.game.Game;
import org.junit.jupiter.api.Test;

class GameRepositoryTest {

    private final GameRepository repository = new GameRepository();

    @Test
    void savedGameIsFoundById() {
        Game game = new Game("ggLmesXI", 3, 0, 0, 0, 0, 0);

        repository.save(game);

        assertThat(repository.findById("ggLmesXI")).contains(game);
    }

    @Test
    void savingAgainReplacesTheState() {
        repository.save(new Game("ggLmesXI", 3, 0, 0, 0, 0, 0));
        repository.save(new Game("ggLmesXI", 2, 45, 1, 210, 210, 9));

        assertThat(repository.findById("ggLmesXI")).map(Game::score).contains(210);
    }

    @Test
    void unknownIdIsEmpty() {
        assertThat(repository.findById("nope1234")).isEmpty();
    }
}
