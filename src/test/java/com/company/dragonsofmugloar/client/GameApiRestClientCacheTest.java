package com.company.dragonsofmugloar.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.company.dragonsofmugloar.config.CacheConfig;
import com.company.dragonsofmugloar.config.GameApiProperties;
import com.company.dragonsofmugloar.domain.shop.ShopItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import io.github.resilience4j.springboot.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import io.github.resilience4j.springboot.retry.autoconfigure.RetryAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(GameApiRestClient.class)
@EnableConfigurationProperties(GameApiProperties.class)
@Import({CacheConfig.class, GameApiRestClientCacheTest.InMemoryCache.class})
@ImportAutoConfiguration({AopAutoConfiguration.class, RateLimiterAutoConfiguration.class, RetryAutoConfiguration.class})
@TestPropertySource(properties = "game-api.base-url=https://game.test/api/v2")
class GameApiRestClientCacheTest {

    @Autowired
    private GameApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void getShopItemsFetchesTheCatalogueOnceForAllGames() {
        server.expect(once(), requestTo("https://game.test/api/v2/ggLmesXI/shop"))
                .andRespond(withSuccess("""
                        [{"id":"hpot","name":"Healing potion","cost":50},{"id":"cs","name":"Claw Sharpening","cost":100}]
                        """, MediaType.APPLICATION_JSON));

        List<ShopItem> first = client.getShopItems("ggLmesXI");
        List<ShopItem> second = client.getShopItems("0NVG7E0r");

        assertThat(first).hasSize(2);
        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class InMemoryCache {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CacheConfig.SHOP_ITEMS);
        }
    }
}
