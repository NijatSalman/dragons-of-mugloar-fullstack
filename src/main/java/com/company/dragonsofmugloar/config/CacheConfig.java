package com.company.dragonsofmugloar.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Turns on Spring's cache abstraction; Boot's in-memory cache manager is enough for the static shop catalogue. */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

    public static final String SHOP_ITEMS = "shop-items";
}
