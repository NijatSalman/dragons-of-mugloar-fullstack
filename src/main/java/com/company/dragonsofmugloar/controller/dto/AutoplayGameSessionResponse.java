package com.company.dragonsofmugloar.controller.dto;

import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSession;
import com.company.dragonsofmugloar.domain.autoplay.AutoplayGameSessionStatus;
import com.company.dragonsofmugloar.domain.autoplay.ScoreSummary;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Progress of an autoplay session; poll until status is FINISHED")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AutoplayGameSessionResponse(
        @Schema(example = "84e1bfd2-3f61-467d-8fe5-90b9bc358e39") String sessionId,
        AutoplayGameSessionStatus status,
        Instant startedAt,
        @Schema(description = "Games requested", example = "3") int requested,
        @Schema(description = "Games finished or failed so far", example = "1") long finished,
        List<AutoplayGameProgressResponse> games,
        @Schema(description = "Scores of finished games; present once at least one game finished") ScoreSummary summary) {

    public static AutoplayGameSessionResponse from(AutoplayGameSession session) {
        return new AutoplayGameSessionResponse(session.sessionId(), session.status(), session.startedAt(), session.games().size(),
                session.endedGamesCount(), session.games().stream().map(AutoplayGameProgressResponse::from).toList(),
                session.scoreSummary().orElse(null));
    }
}
