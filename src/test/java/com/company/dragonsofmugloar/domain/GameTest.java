package com.company.dragonsofmugloar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void gameIsOverWhenNoLivesRemain() {
        assertThat(new Game("abc123", 0, 10, 1, 500, 500, 30).isOver()).isTrue();
    }

    @Test
    void gameContinuesWhileLivesRemain() {
        assertThat(new Game("abc123", 1, 10, 1, 500, 500, 30).isOver()).isFalse();
    }
}
