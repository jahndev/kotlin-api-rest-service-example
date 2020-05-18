package com.example.ps.payu.config

import com.example.ps.payu.clients.interceptors.OutboundRequestInterceptor
import com.example.ps.payu.common.LogContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class PaymentsosRestTemplateConfiguration {

    @Value("\${spring.rest-template.paymentsos-connection-timeout}")
    var connectionTimeout: Long = 10000

    @Bean(name = ["paymentsosRestTemplate"])
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder, logContext: LogContext): RestTemplate =
        restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(connectionTimeout))
            .requestFactory(HttpComponentsClientHttpRequestFactory::class.java)
            .build().apply {
                interceptors = interceptors + OutboundRequestInterceptor(logContext)
            }
}
