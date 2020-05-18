package com.example.ps.payu.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@ConditionalOnProperty(name = ["spring.cache.name"])
@EnableCaching
@Suppress("LateinitUsage")
class CacheHelper {

    @Value("\${spring.cache.name}")
    lateinit var cacheName: String

    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(cacheName)

    @ConditionalOnProperty(name = ["spring.cache.autoexpiry"], value = ["true"])
    @Scheduled(fixedDelayString = "\${spring.cache.expire.delay:600000}")
    fun cacheEvict() = cacheManager().getCache(cacheName)?.clear()
}