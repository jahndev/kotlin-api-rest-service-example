package com.example.ps.payu.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.example.ps.payu.clients.paymentsos.dto.AdditionalDetails

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuNotification(
    val id: String,
    val created: String,
    val paymentId: String,
    val accountId: String,
    val appId: String,
    val data: PayuData
)

data class Notification(
    val eventType: String,
    val examplePaymentId: String,
    val attemptId: String,
    val method: MethodType,
    val status: PayuStatus,
    val integrator: Integrator,
    val psp: String,
    val payuNotification: PayuNotification
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PayuData(
    val id: String,
    val result: PayuResult,
    val providerSpecificData: PayuProviderSpecificData,
    val reconciliationId: String,
    val providerData: PayuProviderData,
    val amount: String,
    val currency: String? = ""
) {
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PayuResult(val status: String, val category: String = "", val subCategory: String = "")
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PayuProviderSpecificData(val additionalDetails: AdditionalDetails)
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PayuProviderData(val responseCode: String)
}

enum class PayuEventType(
    private val eventName: String,
    private val prefix: String = "payment",
    private val eventStatus: String
) {
    CHARGE_UPDATE(eventName = "charge", eventStatus = "create"),
    CHARGE_CREATE(eventName = "charge", eventStatus = "update");

    fun eventType() = "$prefix.$eventName.$eventStatus"
}

enum class PayuStatus {
    PENDING, SUCCEED, FAILED
}
