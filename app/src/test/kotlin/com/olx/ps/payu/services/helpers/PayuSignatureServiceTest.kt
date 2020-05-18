package com.example.ps.payu.services.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.example.ps.payu.clients.paymentsos.PayuCredentialsProvider
import com.example.ps.payu.common.paymentsosCountryCredential
import com.example.ps.payu.model.PayuNotification
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.services.PSP_PAYU
import com.example.ps.payu.web.requests.PayuNotificationRequest
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils

class PayuSignatureServiceTest {
    private val payuCredentialsProvider = mockk<PayuCredentialsProvider>()
    private val payuSignatureService = PayuSignatureService(payuCredentialsProvider)

    @BeforeEach
    fun setUp() {
        every { payuCredentialsProvider.getPaymentsosCredentials(any<String>()) } returns paymentsosCountryCredential
    }

    @Test
    fun `a valid signature is verified correctly`() {
        val signature = "sig1=d569c0a3e8aa62ad35c70ab3bacae484ebc5af10f65bc5b5d59cc5fd295c262d,sig2=b4531f1a16904562ac9c9e938e06c585bde59d3da9e847022b9b2830f5e2e544"
        val jsonBodyRequest = loadNotification("webhook_pending_status.json")
        val payuNotificationRequest = jacksonObjectMapper()
            .readValue<PayuNotificationRequest>(jsonBodyRequest)

        val notification = Notification(
            eventType = "",
            examplePaymentId = "",
            attemptId = "",
            method = MethodType.BANK_TRANSFER,
            status = PayuStatus.PENDING,
            integrator = Integrator.example_CO,
            psp = PSP_PAYU,
            payuNotification = PayuNotification(
                id = payuNotificationRequest.id,
                created = payuNotificationRequest.created,
                paymentId = payuNotificationRequest.paymentId,
                accountId = payuNotificationRequest.accountId,
                appId = payuNotificationRequest.appId,
                data = payuNotificationRequest.data
            )
        )

        val signatureIsValid = payuSignatureService.verifySignature(
            notification = notification,
            eventType = "payment.charge.update",
            signature = signature)

        assertThat(signatureIsValid).isTrue()
    }
}

private fun loadNotification(filename: String) =
    ResourceUtils.getFile("${ResourceUtils.CLASSPATH_URL_PREFIX}$filename").readText()