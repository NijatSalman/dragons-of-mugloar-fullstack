package com.company.dragonsofmugloar.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.TestProperties;
import com.company.dragonsofmugloar.domain.game.Game;
import org.junit.jupiter.api.Test;

class PurchasePolicyTest {

    private final PurchasePolicy policy = new PurchasePolicy(TestProperties.autoplay());

    @Test
    void keepsGoldWhileThereIsTooLittleForAnything() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 120, 0, 130, 130, 6))).isEmpty();
    }

    @Test
    void buysNothingOnceTheGameIsOver() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 0, 900, 2, 3000, 3000, 80))).isEmpty();
    }

    @Test
    void healsFirstWhenLivesAreLow() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 2, 400, 3, 900, 900, 30))).contains("hpot");
    }

    @Test
    void doesNotHealWithoutEnoughGold() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 1, 49, 0, 60, 60, 5))).isEmpty();
    }

    @Test
    void buysCheapUpgradesInOrderByLevel() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 150, 0, 200, 200, 8))).contains("cs");
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 160, 3, 700, 700, 20))).contains("tricks");
    }

    @Test
    void switchesToExpensiveUpgradesAfterAllCheapOnes() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 340, 5, 1500, 1500, 40))).isEmpty();
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 350, 5, 1500, 1500, 40))).contains("ch");
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 900, 9, 4000, 4000, 90))).contains("wingpotmax");
    }

    @Test
    void stopsBuyingOnceEveryUpgradeIsOwned() {
        assertThat(policy.chooseItemToBuy(new Game("0NVG7E0r", 3, 5000, 10, 9000, 9000, 150))).isEmpty();
    }
}
