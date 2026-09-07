package com.company.dragonsofmugloar.repository;

import com.company.dragonsofmugloar.domain.game.Game;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** In-memory store of the games this application has started, keyed by game id. */
@Repository
public class GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public void save(Game game) {
        games.put(game.gameId(), game);
    }

    public Optional<Game> findById(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    /** The finished games with the highest scores, best first, at most {@code limit} of them. */
    public List<Game> findTopFinished(int limit) {
        return games.values().stream()
                .filter(Game::isOver)
                .sorted(Comparator.comparingInt(Game::score).reversed())
                .limit(limit)
                .toList();
    }
}
