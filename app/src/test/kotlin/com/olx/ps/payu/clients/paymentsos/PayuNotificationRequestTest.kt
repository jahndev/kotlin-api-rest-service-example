package com.example.ps.payu.clients.paymentsos

import com.example.ps.payu.clients.paymentsos.dto.AdditionalDetails
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.getPayuData
import com.example.ps.payu.common.getPayuNotification
import com.example.ps.payu.common.EXECUTION_INFO
import com.example.ps.payu.common.BANK_NAME_CO
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.example_PAYMENT_ID
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.model.PayuData
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.web.converters.toPayuNotificationHeaders
import com.example.ps.payu.web.requests.PayuNotificationHeaders
import com.example.ps.payu.web.requests.PayuNotificationRequest
import com.example.ps.payu.web.requests.status
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.HttpMessageConversionException

const val SIGNATURE = "signature"
const val EVENT_TYPE = "event type"
const val VERSION = "payu version"
const val OS_ENV = "os env"
const val REQUEST_ID = "request id"

class PayuNotificationRequestTest {

    private val getPayuNotificationRequest = PayuNotificationRequest(
        id = "",
        created = "",
        paymentId = PAYMENT_ID,
        accountId = "",
        appId = "",
        data = getPayuData("Succeed")
    )

    private val PAYU_NOTIFICATION = getPayuNotification("Succeed")

    private fun createValidHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add(PAYU_SIGNATURE, SIGNATURE)
            add(PAYU_EVENT_TYPE, EVENT_TYPE)
            add(PAYU_VERSION, VERSION)
            add(X_PAYMENTS_OS_ENV, OS_ENV)
            add(X_ZOOZ_REQUEST_ID, REQUEST_ID)
        }

    @ParameterizedTest
    @ValueSource(strings = ["Succeed", "Failed", "Pending"])
    fun `verify status mapping`(status: String) {
        val payuNotificationRequest = getPayuNotificationRequest.copy(data = getPayuData(status))

        assertThat(payuNotificationRequest.status).isEqualTo(PayuStatus.valueOf(status.toUpperCase()))
    }

    @Test
    fun `wrong status mapping produce IllegalArgumentException`() {
        val payuNotificationRequest = getPayuNotificationRequest
            .copy(data = getPayuData("unknown"))

        assertThrows<IllegalArgumentException> { payuNotificationRequest.status }
    }

    @Test
    fun `verify header mapping`() {
        val result = createValidHeaders().toPayuNotificationHeaders()
        assertThat(result).isEqualTo(PayuNotificationHeaders(SIGNATURE, EVENT_TYPE, VERSION, OS_ENV, REQUEST_ID))
    }

    @ParameterizedTest
    @ValueSource(strings = [PAYU_SIGNATURE, PAYU_EVENT_TYPE, PAYU_VERSION, X_PAYMENTS_OS_ENV, X_ZOOZ_REQUEST_ID])
    fun `each header value is mandatory`(requiredHeader: String) {
        val headers = createValidHeaders()
        headers.remove(requiredHeader)
        assertThrows<HttpMessageConversionException> { headers.toPayuNotificationHeaders() }
    }

    @Test
    fun `verify additional details mapping`() {
        val additionalDetails = AdditionalDetails(
            bankTransferFinancialInstitutionCode = EXECUTION_INFO.bankCode,
            bankTransferFinancialInstitutionName = BANK_NAME_CO,
            bankTransferPaymentMethodVendor = BANK_TRANSFER_VENDOR_PSE,
            nationalIdentifyType = EXECUTION_INFO.identificationType,
            nationalIdentifyNumber = EXECUTION_INFO.identificationNumber,
            customerNationalIdentifyNumber = EXECUTION_INFO.identificationNumber,
            merchantPayerId = PAYMENT.customer.userId,
            payerEmail = PAYMENT.customer.emailAddress.toString(),
            orderLanguage = PAYMENT.integratorEnum().country.languages.first().language,
            paymentCountry = PAYMENT.integratorEnum().country.countryCodeAlpha3,
            userType = EXECUTION_INFO.userType,
            examplePaymentId = example_PAYMENT_ID
        )

        val payuData = PAYU_NOTIFICATION.data.copy(
            providerSpecificData = PayuData.PayuProviderSpecificData(additionalDetails = additionalDetails)
        )

        val payuNotification = PAYU_NOTIFICATION.copy(data = payuData)

        val additionalDetailsSpected = payuNotification.data.providerSpecificData.additionalDetails

        assertThat(additionalDetailsSpected).isEqualTo(additionalDetails)
    }
}
