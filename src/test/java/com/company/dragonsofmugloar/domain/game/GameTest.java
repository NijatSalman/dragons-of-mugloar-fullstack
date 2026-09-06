package com.company.dragonsofmugloar.domain.game;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GameTest {

    @Test
    void isOverReturnsTrueWhenNoLivesRemain() {
        assertThat(new Game("ggLmesXI", 0, 87, 3, 1462, 1462, 41, GameOrigin.MANUAL).isOver()).isTrue();
    }

    @Test
    void isOverReturnsFalseWhileLivesRemain() {
        assertThat(new Game("ggLmesXI", 1, 87, 3, 1462, 1462, 41, GameOrigin.MANUAL).isOver()).isFalse();
    }
}
