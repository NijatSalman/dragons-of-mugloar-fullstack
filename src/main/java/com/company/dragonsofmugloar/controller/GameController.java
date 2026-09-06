package com.company.dragonsofmugloar.controller;

import com.company.dragonsofmugloar.controller.dto.AdResponse;
import com.company.dragonsofmugloar.controller.dto.GameResponse;
import com.company.dragonsofmugloar.controller.dto.ReputationResponse;
import com.company.dragonsofmugloar.controller.dto.SolveResultResponse;
import com.company.dragonsofmugloar.service.AdService;
import com.company.dragonsofmugloar.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Start a game, read its state, pick and solve ads")
public class GameController {

    /** Ids issued by the game server are short alphanumeric strings. */
    static final String ID_PATTERN = "[A-Za-z0-9]{1,64}";

    private final GameService gameService;
    private final AdService adService;

    @PostMapping
    @Operation(summary = "Start a new game")
    @ApiResponse(responseCode = "201", description = "Game started")
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse startGame() {
        return GameResponse.from(gameService.startGame());
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Current state of a game as last seen by this application")
    @ApiResponse(responseCode = "404", description = "Unknown game")
    public GameResponse getGame(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return GameResponse.from(gameService.getGame(gameId));
    }

    @GetMapping("/{gameId}/ads")
    @Operation(summary = "Ads on the board, best first, each with chance, expected value and a recommendation")
    @ApiResponse(responseCode = "410", description = "Game is over")
    public List<AdResponse> getRecommendedAds(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return adService.getRecommendedAds(gameId).stream().map(AdResponse::from).toList();
    }

    @PostMapping("/{gameId}/ads/{adId}/solve")
    @Operation(summary = "Attempt an ad; costs one turn and, on failure, one life")
    @ApiResponse(responseCode = "409", description = "Ad no longer exists")
    public SolveResultResponse solveAd(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId,
                               @PathVariable @Pattern(regexp = ID_PATTERN) String adId) {
        return SolveResultResponse.from(adService.solveAd(gameId, adId));
    }

    @PostMapping("/{gameId}/reputation")
    @Operation(summary = "Investigate reputation; costs one turn")
    public ReputationResponse investigateReputation(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return ReputationResponse.from(gameService.investigateReputation(gameId));
    }
}
