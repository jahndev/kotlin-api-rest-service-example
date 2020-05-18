package com.example.ps.payu.services.helpers

import com.example.example.payment.client.dto.Payment
import com.example.ps.payu.clients.paymentsos.PAYU_TOKENIZATION_TYPE
import com.example.ps.payu.clients.paymentsos.dto.AdditionalDetails
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuAddress
import com.example.ps.payu.clients.paymentsos.dto.PayuBillingAddress
import com.example.ps.payu.clients.paymentsos.dto.PayuChargeRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuItem
import com.example.ps.payu.clients.paymentsos.dto.PayuOrder
import com.example.ps.payu.clients.paymentsos.dto.PayuPaymentMethod
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.PSP_PAYU
import org.springframework.stereotype.Component

@Component
class PaymentsosRequestFactory(
    private val urlProvider: UrlProvider,
    private val payuBankService: PayuBankService
) {

    fun createPaymentRequest(payment: Payment) =
        with(payment) {
            PaymentsosPaymentRequest(
                amount = amount.grossValue.movePointRight(2).toLong(),
                currency = amount.currency,
                statementSoftDescriptor = order.description,
                order = PayuOrder(
                    id = order.reference,
                    lineItems = listOf(
                        PayuItem(
                            name = order.reference,
                            unitPrice = amount.grossValue.movePointRight(2).toInt()
                        )
                    )
                ),
                shippingAddress = with(customer) {
                    PayuAddress(
                        firstName = firstName,
                        lastName = lastName,
                        email = emailAddress,
                        phone = phoneNumber
                    )
                },
                billingAddress = PayuBillingAddress(
                    country = Integrator.valueOf(integrator).country.countryCodeAlpha3,
                    email = customer.emailAddress
                )
            )
        }

    fun createChargeRequest(
        payment: Payment,
        attemptId: String,
        methodType: MethodType,
        executionInfo: ExecutionInfo
    ): PayuChargeRequest {
        val integrator = payment.integratorEnum()
        return PayuChargeRequest(
            merchantSiteUrl = urlProvider.createRedirectUrl(
                attemptId = attemptId,
                integrator = integrator,
                paymentId = payment.id,
                methodType = methodType
            ),
            paymentMethod = PayuPaymentMethod(
                sourceType = methodType.name,
                type = PAYU_TOKENIZATION_TYPE,
                vendor = PSP_PAYU,
                additionalDetails = AdditionalDetails(
                    bankTransferFinancialInstitutionCode = executionInfo.bankCode,
                    bankTransferFinancialInstitutionName = getBankName(integrator, executionInfo.bankCode),
                    bankTransferPaymentMethodVendor = BANK_TRANSFER_VENDOR_PSE,
                    nationalIdentifyType = executionInfo.identificationType,
                    nationalIdentifyNumber = executionInfo.identificationNumber,
                    customerNationalIdentifyNumber = executionInfo.identificationNumber,
                    merchantPayerId = payment.customer.userId,
                    payerEmail = payment.customer.emailAddress,
                    orderLanguage = integrator.country.languages.first().language,
                    paymentCountry = integrator.country.countryCodeAlpha3,
                    userType = executionInfo.userType,
                    examplePaymentId = payment.id
                )
            ),
            reconciliationId = attemptId
        )
    }

    fun getBankName(integrator: Integrator, pseCode: String) = payuBankService.getBankList(integrator)
        .first { it.pseCode == pseCode }.description
}
