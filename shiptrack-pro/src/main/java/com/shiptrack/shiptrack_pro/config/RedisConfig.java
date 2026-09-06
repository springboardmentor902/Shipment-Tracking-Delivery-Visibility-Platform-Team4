package com.shiptrack.shiptrack_pro.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {

    /*
     * Redis configuration is kept in the project through:
     * - spring-boot-starter-data-redis dependency
     * - application.properties Redis settings
     *
     * For local development, Redis server is not currently running.
     * Therefore, use an in-memory cache so the application continues
     * working without requiring Redis.
     *
     * Analytics @Cacheable annotations continue to work.
     */
    @Bean
    public CacheManager cacheManager() {

        ConcurrentMapCacheManager cacheManager =
                new ConcurrentMapCacheManager(
                        "customerAnalytics",
                        "businessAnalytics",
                        "adminAnalytics"
                );

        return cacheManager;
    }
}