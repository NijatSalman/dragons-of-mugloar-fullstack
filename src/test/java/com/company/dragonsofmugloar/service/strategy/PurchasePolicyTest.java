package com.company.dragonsofmugloar.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.TestProperties;
import com.company.dragonsofmugloar.domain.game.Game;
import com.company.dragonsofmugloar.domain.game.GameOrigin;
import org.junit.jupiter.api.Test;

class PurchasePolicyTest {

    private final PurchasePolicy policy = new PurchasePolicy(TestProperties.autoplay());

    @Test
    void chooseItemToBuyReturnsEmptyWhenGoldIsTooLow() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 120, 0, 130, 130, 6, GameOrigin.MANUAL))).isEmpty();
    }

    @Test
    void chooseItemToBuyReturnsEmptyWhenGameIsOver() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 0, 900, 2, 3000, 3000, 80, GameOrigin.MANUAL))).isEmpty();
    }

    @Test
    void chooseItemToBuyPicksThePotionWhenLivesAreLow() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 2, 400, 3, 900, 900, 30, GameOrigin.MANUAL))).contains("hpot");
    }

    @Test
    void chooseItemToBuyReturnsEmptyWhenPotionIsUnaffordable() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 1, 49, 0, 60, 60, 5, GameOrigin.MANUAL))).isEmpty();
    }

    @Test
    void chooseItemToBuyPicksCheapUpgradesInOrderByLevel() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 150, 0, 200, 200, 8, GameOrigin.MANUAL))).contains("cs");
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 160, 3, 700, 700, 20, GameOrigin.MANUAL))).contains("tricks");
    }

    @Test
    void chooseItemToBuyPicksExpensiveUpgradesAfterAllCheapOnes() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 340, 5, 1500, 1500, 40, GameOrigin.MANUAL))).isEmpty();
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 350, 5, 1500, 1500, 40, GameOrigin.MANUAL))).contains("ch");
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 900, 9, 4000, 4000, 90, GameOrigin.MANUAL))).contains("wingpotmax");
    }

    @Test
    void chooseItemToBuyReturnsEmptyWhenEveryUpgradeIsOwned() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 5000, 10, 9000, 9000, 150, GameOrigin.MANUAL))).isEmpty();
    }
}
