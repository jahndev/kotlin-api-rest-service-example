package com.example.ps.payu.config

import com.example.ps.payu.clients.interceptors.OutboundRequestInterceptor
import com.example.ps.payu.common.LogContext
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {
    @Bean
    @Primary
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder, logContext: LogContext): RestTemplate =
        restTemplateBuilder
            .requestFactory(HttpComponentsClientHttpRequestFactory::class.java)
            .build().apply {
                interceptors = interceptors + OutboundRequestInterceptor(logContext)
            }
}
