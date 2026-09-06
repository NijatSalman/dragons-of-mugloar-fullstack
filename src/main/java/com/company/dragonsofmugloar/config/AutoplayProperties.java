package com.company.dragonsofmugloar.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Every threshold the autoplayer decides with, so the strategy can be tuned in {@code application.yaml}
 * without touching code.
 *
 * @param recommendedMinChance   an ad below this chance is never recommended
 * @param recommendedMinTurnsLeft an ad expiring sooner than this is never recommended
 * @param lowLives               at or below this many lives the bot plays safe and buys potions
 * @param safeChance             the minimum chance accepted while playing safe
 * @param potionItem             shop id of the healing potion
 * @param potionMinGold          gold needed before a potion is bought
 * @param cheapUpgradeMinGold    gold needed before the next cheap upgrade is bought
 * @param cheapUpgrades          cheap upgrades in purchase order, one per dragon level
 * @param expensiveUpgradeMinGold gold needed before the next expensive upgrade is bought
 * @param expensiveUpgrades      expensive upgrades in purchase order, after all cheap ones
 * @param maxTurns               hard stop so a misbehaving server can never keep a game running forever
 */
@Validated
@ConfigurationProperties(prefix = "autoplay")
public record AutoplayProperties(
        @DecimalMin("0") @DecimalMax("1") double recommendedMinChance,
        @Min(1) int recommendedMinTurnsLeft,
        @Min(0) int lowLives,
        @DecimalMin("0") @DecimalMax("1") double safeChance,
        @NotBlank String potionItem,
        @Min(0) int potionMinGold,
        @Min(0) int cheapUpgradeMinGold,
        @NotEmpty List<String> cheapUpgrades,
        @Min(0) int expensiveUpgradeMinGold,
        @NotEmpty List<String> expensiveUpgrades,
        @Min(1) int maxTurns) {
}
