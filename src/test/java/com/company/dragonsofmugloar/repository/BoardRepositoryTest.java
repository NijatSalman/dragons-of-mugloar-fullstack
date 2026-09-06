package com.company.dragonsofmugloar.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.dragonsofmugloar.domain.ad.Ad;
import com.company.dragonsofmugloar.domain.ad.Probability;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoardRepositoryTest {

    private static final Ad AD = new Ad("DSAUBsXa", "Help Majid Desprez to transport a magic beer mug to steppe in Falldean",
            21, 7, Probability.QUITE_LIKELY);

    private final BoardRepository repository = new BoardRepository();

    @Test
    void findAdReturnsTheAdFromTheLastSavedBoard() {
        repository.save("ggLmesXI", List.of(AD));

        assertThat(repository.findAd("ggLmesXI", "DSAUBsXa")).contains(AD);
    }

    @Test
    void findAdReturnsEmptyWhenAdOrGameIsUnknown() {
        repository.save("ggLmesXI", List.of(AD));

        assertThat(repository.findAd("ggLmesXI", "HiCtYxHC")).isEmpty();
        assertThat(repository.findAd("0NVG7E0r", "DSAUBsXa")).isEmpty();
    }

    @Test
    void deleteForgetsTheBoard() {
        repository.save("ggLmesXI", List.of(AD));

        repository.delete("ggLmesXI");

        assertThat(repository.findAd("ggLmesXI", "DSAUBsXa")).isEmpty();
    }
}
