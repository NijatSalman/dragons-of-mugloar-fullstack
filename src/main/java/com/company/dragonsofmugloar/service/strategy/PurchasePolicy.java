package com.company.dragonsofmugloar.service.strategy;

import com.company.dragonsofmugloar.config.AutoplayProperties;
import com.company.dragonsofmugloar.domain.game.Game;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Decides what to buy after a turn, if anything. Staying alive comes first, then dragon upgrades in a fixed
 * order: the dragon's level tells how many upgrades it already has, so the policy needs no memory of its own.
 */
@Component
@RequiredArgsConstructor
public class PurchasePolicy {

    private final AutoplayProperties properties;

    /** The shop item to buy now, or empty to keep the gold. Nothing is bought once the game is over. */
    public Optional<String> chooseItemToBuy(Game game) {
        if (game.isOver()) {
            return Optional.empty();
        }
        return potionIfNeeded(game).or(() -> nextUpgrade(game));
    }

    private Optional<String> potionIfNeeded(Game game) {
        boolean lowOnLives = game.lives() <= properties.lowLives();
        boolean canAfford = game.gold() >= properties.potionMinGold();
        return lowOnLives && canAfford ? Optional.of(properties.potionItem()) : Optional.empty();
    }

    private Optional<String> nextUpgrade(Game game) {
        List<String> cheap = properties.cheapUpgrades();
        return game.level() < cheap.size()
                ? ifAffordable(cheap.get(game.level()), properties.cheapUpgradeMinGold(), game)
                : nextExpensiveUpgrade(game, game.level() - cheap.size());
    }

    private Optional<String> nextExpensiveUpgrade(Game game, int index) {
        List<String> expensive = properties.expensiveUpgrades();
        return index < expensive.size()
                ? ifAffordable(expensive.get(index), properties.expensiveUpgradeMinGold(), game)
                : Optional.empty();
    }

    private static Optional<String> ifAffordable(String itemId, int minGold, Game game) {
        return game.gold() >= minGold ? Optional.of(itemId) : Optional.empty();
    }
}
