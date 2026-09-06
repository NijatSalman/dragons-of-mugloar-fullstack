package com.company.dragonsofmugloar.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.company.dragonsofmugloar.config.GameApiProperties;
import io.github.resilience4j.springboot.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

/** Five permits per second here, so ten calls must span at least one refresh period. */
@RestClientTest(GameApiRestClient.class)
@EnableConfigurationProperties(GameApiProperties.class)
@ImportAutoConfiguration({AopAutoConfiguration.class, RateLimiterAutoConfiguration.class, RetryAutoConfiguration.class})
@TestPropertySource(properties = {
        "game-api.base-url=https://game.test/api/v2",
        "resilience4j.ratelimiter.instances.gameApi.limit-for-period=5",
        "resilience4j.ratelimiter.instances.gameApi.limit-refresh-period=1s",
        "resilience4j.ratelimiter.instances.gameApi.timeout-duration=10s"})
class GameApiRestClientRateLimitTest {

    @Autowired
    private GameApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void callsBeyondThePermitsPerSecondWaitForTheNextPeriod() {
        server.expect(times(10), requestTo("https://game.test/api/v2/ggLmesXI/messages"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        Instant start = Instant.now();

        for (int call = 0; call < 10; call++) {
            client.getAds("ggLmesXI");
        }

        assertThat(Duration.between(start, Instant.now())).isGreaterThanOrEqualTo(Duration.ofMillis(900));
        server.verify();
    }
}
