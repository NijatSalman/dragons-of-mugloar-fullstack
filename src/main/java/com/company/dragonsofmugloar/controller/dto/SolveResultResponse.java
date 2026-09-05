package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.SolveResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Outcome of attempting an ad")
public record SolveResultResponse(
        boolean success,
        @Schema(example = "3") int lives,
        @Schema(example = "25") int gold,
        @Schema(example = "25") int score,
        @Schema(example = "1462") int highScore,
        @Schema(example = "2") int turn,
        @Schema(example = "You successfully solved the mission!") String message) {

    public static SolveResultResponse from(SolveResult result) {
        return new SolveResultResponse(result.success(), result.lives(), result.gold(), result.score(), result.highScore(),
                result.turn(), result.message());
    }
}
