package com.company.dragonsofmugloar.service;

import com.company.dragonsofmugloar.client.GameApiClient;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import com.company.dragonsofmugloar.domain.game.Reputation;
import com.company.dragonsofmugloar.exception.GameNotFoundException;
import com.company.dragonsofmugloar.repository.GameRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** A game as a whole: start it, read its current state, investigate reputation. */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameApiClient gameApiClient;
    private final GameRepository gameRepository;

    public Game startGame(GameOrigin origin) {
        Game game = gameApiClient.startGame(origin);
        gameRepository.save(game);
        log.info("Game started: gameId={}, origin={}, lives={}, gold={}", game.gameId(), origin, game.lives(), game.gold());
        return game;
    }

    /** The leaderboard: finished games, manual and autoplay alike, highest score first. */
    public List<Game> listTopFinishedGames(int limit) {
        return gameRepository.findTopFinished(limit);
    }

    public Game getGame(String gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    /** Costs one turn on the game server. */
    public Reputation investigateReputation(String gameId) {
        getGame(gameId);
        Reputation reputation = gameApiClient.investigateReputation(gameId);
        log.debug("Reputation investigated: gameId={}, people={}, state={}, underworld={}",
                gameId, reputation.people(), reputation.state(), reputation.underworld());
        return reputation;
    }
}
