package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state of a game")
public record GameResponse(
        @Schema(example = "ggLmesXI") String gameId,
        @Schema(example = "3") int lives,
        @Schema(example = "120") int gold,
        @Schema(example = "2") int level,
        @Schema(example = "1462") int score,
        @Schema(example = "1462") int highScore,
        @Schema(example = "41") int turn,
        @Schema(description = "True once no lives remain") boolean over) {

    public static GameResponse from(Game game) {
        return new GameResponse(game.gameId(), game.lives(), game.gold(), game.level(), game.score(),
                game.highScore(), game.turn(), game.isOver());
    }
}
