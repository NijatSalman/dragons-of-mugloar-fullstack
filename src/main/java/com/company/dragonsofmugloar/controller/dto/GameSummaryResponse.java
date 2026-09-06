package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One row of the leaderboard")
public record GameSummaryResponse(
        @Schema(example = "jz21oOWI") String gameId,
        GameOrigin origin,
        @Schema(example = "5239") int score,
        @Schema(example = "201") int turn,
        @Schema(example = "0") int lives,
        @Schema(example = "87") int gold,
        @Schema(example = "3") int level,
        @Schema(description = "True once no lives remain") boolean over) {

    public static GameSummaryResponse from(Game game) {
        return new GameSummaryResponse(game.gameId(), game.origin(), game.score(), game.turn(), game.lives(), game.gold(),
                game.level(), game.isOver());
    }
}
