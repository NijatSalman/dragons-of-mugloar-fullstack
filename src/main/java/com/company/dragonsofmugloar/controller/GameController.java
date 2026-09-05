package com.company.dragonsofmugloar.controller;

import com.company.dragonsofmugloar.controller.dto.AdResponse;
import com.company.dragonsofmugloar.controller.dto.GameResponse;
import com.company.dragonsofmugloar.controller.dto.ReputationResponse;
import com.company.dragonsofmugloar.controller.dto.SolveResultResponse;
import com.company.dragonsofmugloar.domain.Game;
import com.company.dragonsofmugloar.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Start a game, read its state, pick and solve ads")
public class GameController {

    /** Ids issued by the game server are short alphanumeric strings. */
    static final String ID_PATTERN = "[A-Za-z0-9]{1,64}";

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "Start a new game")
    @ApiResponse(responseCode = "201", description = "Game started")
    public ResponseEntity<GameResponse> startGame() {
        Game game = gameService.startGame();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{gameId}")
                .buildAndExpand(game.gameId()).toUri();
        return ResponseEntity.created(location).body(GameResponse.from(game));
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
    public List<AdResponse> getAds(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return gameService.getAds(gameId).stream().map(AdResponse::from).toList();
    }

    @PostMapping("/{gameId}/ads/{adId}/solve")
    @Operation(summary = "Attempt an ad; costs one turn and, on failure, one life")
    @ApiResponse(responseCode = "409", description = "Ad no longer exists")
    public SolveResultResponse solve(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId,
                               @PathVariable @Pattern(regexp = ID_PATTERN) String adId) {
        return SolveResultResponse.from(gameService.solve(gameId, adId));
    }

    @PostMapping("/{gameId}/reputation")
    @Operation(summary = "Investigate reputation; costs one turn")
    public ReputationResponse investigateReputation(@PathVariable @Pattern(regexp = ID_PATTERN) String gameId) {
        return ReputationResponse.from(gameService.investigateReputation(gameId));
    }
}
