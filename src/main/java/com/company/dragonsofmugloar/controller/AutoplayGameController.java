package com.company.dragonsofmugloar.controller;

import com.company.dragonsofmugloar.controller.dto.AutoplayGameSessionResponse;
import com.company.dragonsofmugloar.service.AutoplayGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/autoplay/sessions")
@RequiredArgsConstructor
@Tag(name = "Autoplay", description = "Let the dragon play whole games by itself, several at a time")
public class AutoplayGameController {

    /** Upper bound per request; all games share the client's rate limiter, so more games only means slower games. */
    static final int MAX_GAMES = 20;
    private static final String UUID_PATTERN = "[0-9a-fA-F-]{36}";

    private final AutoplayGameService autoplayGameService;

    @PostMapping
    @Operation(summary = "Start playing the given number of games in parallel; returns at once")
    @ApiResponse(responseCode = "202", description = "Session accepted; poll GET /{sessionId} for progress")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AutoplayGameSessionResponse startGames(
            @Parameter(description = "How many games to play at once, 1 to 20", example = "3")
            @RequestParam(defaultValue = "1") @Min(1) @Max(MAX_GAMES) int games) {
        return AutoplayGameSessionResponse.from(autoplayGameService.startGames(games));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Progress and scores of a session")
    @ApiResponse(responseCode = "404", description = "Unknown session")
    public AutoplayGameSessionResponse getGamesProgress(@PathVariable @Pattern(regexp = UUID_PATTERN) String sessionId) {
        return AutoplayGameSessionResponse.from(autoplayGameService.getGamesProgress(sessionId));
    }
}
