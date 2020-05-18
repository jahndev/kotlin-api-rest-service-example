package com.example.ps.payu.clients.paymentsos.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.example.ps.payu.model.PayuData
import java.net.URI

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PaymentsosCreatePaymentResponse(
    val id: String
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentsosChargeResponse(
    val redirection: Redirection?,
    @JsonProperty("result")
    val result: PayuData.PayuResult?,
    @JsonProperty("provider_specific_data")
    val providerSpecificData: PayuData.PayuProviderSpecificData?,
    @JsonProperty("provider_data")
    val providerData: PayuProviderData?
)

data class Redirection(val url: URI? = null)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuProviderData(
    val responseCode: String,
    val description: String,
    @JsonRawValue
    val rawResponseJson: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PayuRawResponse(
    var responseMessage: String? = null
)
