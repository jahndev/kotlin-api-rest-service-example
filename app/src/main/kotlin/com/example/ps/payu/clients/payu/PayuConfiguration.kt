package com.example.ps.payu.clients.payu

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Suppress("LateinitUsage")
@ConfigurationProperties(prefix = "services.payu")
@Component
class PayuConfiguration {
    lateinit var url: String
    lateinit var version: String
}