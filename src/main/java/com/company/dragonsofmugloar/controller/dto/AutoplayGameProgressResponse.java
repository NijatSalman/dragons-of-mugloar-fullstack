package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameProgress;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Progress of one game in a session")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AutoplayGameProgressResponse(
        @Schema(example = "jz21oOWI") String gameId,
        AutoplayGameStatus status,
        @Schema(example = "5239") int score,
        @Schema(example = "201") int turn,
        @Schema(example = "0") int lives,
        @Schema(example = "87") int gold,
        @Schema(example = "3") int level,
        @Schema(description = "Only present when the game failed") String error) {

    public static AutoplayGameProgressResponse from(AutoplayGameProgress outcome) {
        return new AutoplayGameProgressResponse(outcome.gameId(), outcome.status(), outcome.score(), outcome.turn(),
                outcome.lives(), outcome.gold(), outcome.level(), outcome.error());
    }
}
