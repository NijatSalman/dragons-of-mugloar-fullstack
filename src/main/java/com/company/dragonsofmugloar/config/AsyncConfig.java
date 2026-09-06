package com.company.dragonsofmugloar.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Executor for autoplay games: one virtual thread per game, so parallel games cost almost nothing. */
@Configuration(proxyBeanMethods = false)
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    ExecutorService autoplayExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
