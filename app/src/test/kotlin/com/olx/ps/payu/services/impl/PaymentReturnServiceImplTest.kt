package com.example.ps.payu.services.impl

import com.example.example.authentication.client.AuthenticationClient
import com.example.example.authentication.client.dto.JwtAuthenticationResponse
import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Attempt
import com.example.example.payment.client.dto.Status
import com.example.example.payment.client.dto.Status.CAPTURED
import com.example.example.payment.client.dto.Status.ERROR
import com.example.example.payment.client.dto.Status.REJECTED
import com.example.ps.payu.common.API_GATEWAY_BASE_URL
import com.example.ps.payu.common.FRONTEND_BASE_URL
import com.example.ps.payu.common.ATTEMPT
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.model.PayuStatus.FAILED
import com.example.ps.payu.model.PayuStatus.PENDING
import com.example.ps.payu.services.PaymentReturnService
import com.example.ps.payu.services.STATUS
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.URI

class PaymentReturnServiceImplTest {

    private val attemptClient = mockk<AttemptClient>()
    private val paymentClient = mockk<PaymentClient>()
    private val authenticationClient = mockk<AuthenticationClient>()

    private val urlProvider = UrlProvider(
        authenticationClient = authenticationClient,
        apiGatewayBaseUrl = API_GATEWAY_BASE_URL,
        frontendBaseUrl = FRONTEND_BASE_URL
    )

    private val paymentReturnService: PaymentReturnService =
        PaymentReturnServiceImpl(
            attemptClient = attemptClient,
            paymentClient = paymentClient,
            urlProvider = urlProvider
            )

    @BeforeEach
    fun setUp() {
        every {
            paymentClient.get(PAYMENT_ID)
        }.returns(PAYMENT)

        every {
            authenticationClient.generateToken(any())
        }.returns(JwtAuthenticationResponse("superToken"))
    }

    @Test
    fun `redirects user to experienceCompleted URL, if any attempt has been CAPTURED`() {
        prepareAttemptClientToReturn(
            listOf(
                ATTEMPT.copy(status = REJECTED),
                ATTEMPT.copy(status = CAPTURED)
            )
        )

        val url = paymentReturnService.handleUserReturn(PAYMENT_ID, ATTEMPT_ID, PENDING)

        assertThat(url.toString()).isEqualTo(PAYMENT.redirectUrls.experienceCompleted)
    }

    @ParameterizedTest
    @EnumSource(value = Status::class, names = ["REJECTED", "ERROR"])
    fun `redirects user to payment selection page if no one has status REJECTED or ERROR`(status: Status) {
        prepareAttemptClientToReturn(
            listOf(
                ATTEMPT.copy(status = Status.PENDING_EXECUTION),
                ATTEMPT.copy(id = ATTEMPT_ID, status = status)
            )
        )

        val url = paymentReturnService.handleUserReturn(PAYMENT_ID, ATTEMPT_ID, PENDING)
        assertIsPaymentSelectionPage(url)
    }

    @Test
    fun `redirects user if the payu status is SUCCEED`() {
        prepareAttemptClientToReturn(listOf(ATTEMPT))

        every {
            attemptClient.update(attemptId = ATTEMPT_ID, patch = mapOf(STATUS to CAPTURED.name))
        } returns Unit

        val url = paymentReturnService.handleUserReturn(PAYMENT_ID, ATTEMPT_ID, PayuStatus.SUCCEED)
        assertThat(url.toString()).isEqualTo(PAYMENT.redirectUrls.experienceCompleted)
    }

    @Test
    fun `redirects user to polling page if payu status is PENDING`() {
        prepareAttemptClientToReturn(listOf(ATTEMPT))

        every {
            attemptClient.update(attemptId = ATTEMPT_ID, patch = mapOf(STATUS to Status.PENDING_EXECUTION.name))
        } returns Unit

        val url = paymentReturnService.handleUserReturn(PAYMENT_ID, ATTEMPT_ID, PENDING).toString()
        assertThat(url).startsWith(FRONTEND_BASE_URL)
        assertThat(url).contains("/payment-result")
        assertThat(url).contains("init=")
        assertThat(url).contains("attemptId=$ATTEMPT_ID")
        assertThat(url).contains("token=superToken")
    }

    @Test
    fun `redirects user to payment selection page if payu status is FAILED`() {
        prepareAttemptClientToReturn(listOf(ATTEMPT))

        every {
            attemptClient.update(attemptId = ATTEMPT_ID, patch = mapOf(STATUS to Status.ERROR.name))
        } returns Unit

        val url = paymentReturnService.handleUserReturn(PAYMENT_ID, ATTEMPT_ID, FAILED)
        assertIsPaymentSelectionPage(url)
    }

    private fun prepareAttemptClientToReturn(attempts: Collection<Attempt>) {
        every {
            attemptClient.getAttempts(PAYMENT_ID, listOf(CAPTURED, ERROR, REJECTED))
        }.returns(attempts)
    }

    private fun assertIsPaymentSelectionPage(uri: URI) {
        val s = uri.toString()
        assertThat(s).startsWith(FRONTEND_BASE_URL)
        assertThat(s).contains("action=UNKNOWN")
        assertThat(s).contains("init=")
        assertThat(s).contains("token=superToken")
    }
}