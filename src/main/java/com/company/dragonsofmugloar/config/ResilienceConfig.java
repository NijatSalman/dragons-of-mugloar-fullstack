package com.company.dragonsofmugloar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

/**
 * Turns on Spring Framework's {@code @Retryable} support used by the game API client.
 * Class-based proxies are required because the annotations live on the implementation's methods,
 * not on the {@code GameApiClient} interface.
 */
@Configuration(proxyBeanMethods = false)
@EnableResilientMethods(proxyTargetClass = true)
public class ResilienceConfig {
}
