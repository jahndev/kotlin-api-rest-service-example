package com.example.ps.payu.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Suppress("LateinitUsage")
@ConfigurationProperties(prefix = "pse")
@Component
class PseProvider {
    var transactionMinValue: Long = 1600
}
