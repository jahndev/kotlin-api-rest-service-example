package com.example.ps.payu.web.requests

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.example.ps.payu.model.PayuData
import com.example.ps.payu.model.PayuStatus

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PayuNotificationRequest(
    val id: String,
    val created: String,
    val paymentId: String,
    val accountId: String,
    val appId: String,
    val data: PayuData
)

val PayuNotificationRequest.status get() = data.result.let { PayuStatus.valueOf(it.status.toUpperCase()) }

data class PayuNotificationHeaders(
    val signature: String,
    val eventType: String,
    val version: String,
    val paymentsOsEnv: String,
    val zoozRequestId: String
)
