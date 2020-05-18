package com.example.ps.payu.clients.paymentsos.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.net.URI
import java.util.Currency

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PaymentsosPaymentRequest(
    val amount: Long,
    val currency: Currency,
    val statementSoftDescriptor: String,
    val order: PayuOrder,
    val shippingAddress: PayuAddress?,
    val billingAddress: PayuBillingAddress?
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuOrder(val id: String, val lineItems: List<PayuItem>)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Int
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuAddress(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuBillingAddress(
    val country: String,
    val email: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuChargeRequest(
    val merchantSiteUrl: URI,
    val paymentMethod: PayuPaymentMethod,
    val reconciliationId: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class PayuPaymentMethod(
    val sourceType: String,
    val type: String,
    val vendor: String,
    val additionalDetails: AdditionalDetails
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AdditionalDetails(
    val bankTransferFinancialInstitutionCode: String,
    val bankTransferFinancialInstitutionName: String,
    val bankTransferPaymentMethodVendor: String,
    val nationalIdentifyType: String,
    val nationalIdentifyNumber: String,
    val customerNationalIdentifyNumber: String,
    val merchantPayerId: String,
    val payerEmail: String?,
    val orderLanguage: String,
    val paymentCountry: String,
    val userType: String,
    val examplePaymentId: String
)