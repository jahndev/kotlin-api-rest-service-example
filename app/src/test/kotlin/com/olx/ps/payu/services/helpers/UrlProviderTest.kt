package com.example.ps.payu.services.helpers

import com.example.example.authentication.client.AuthenticationClient
import com.example.example.authentication.client.dto.JwtAuthenticationResponse
import com.example.example.payment.hateoas.api.v2_1.UserMessage.RETRY_METHOD
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.API_GATEWAY_BASE_URL
import com.example.ps.payu.common.FRONTEND_BASE_URL
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType.BANK_TRANSFER
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class UrlProviderTest {
    private val authenticationClient = mockk<AuthenticationClient>()
    private val urlProvider = UrlProvider(
        authenticationClient,
        API_GATEWAY_BASE_URL,
        FRONTEND_BASE_URL
    )

    @BeforeEach
    fun setUp() {
        every {
            authenticationClient.generateToken(any())
        } returns JwtAuthenticationResponse("a token")
    }

    @Test
    fun `generic page url creation`() {
        val url = urlProvider.createInitializationPageUrl(PAYMENT, BANK_TRANSFER)

        assertThat(url.toASCIIString()).startsWith("/co/bank_transfer")
        assertThat(url.queryParams())
            .containsKeys("init", "token")
            .containsEntry("act", listOf("initialize"))
            .containsEntry("psp", listOf("payu"))
    }

    @Test
    fun `redirect url creation`() {
        urlProvider.createRedirectUrl(Integrator.example_CO, PAYMENT_ID, ATTEMPT_ID, BANK_TRANSFER)
            .apply {
                assertThat(this.toASCIIString())
                    .isEqualTo("$API_GATEWAY_BASE_URL/example_co/bank_transfer/payu/redirect/${PAYMENT.id}/$ATTEMPT_ID")
            }
    }

    @Test
    fun `execution url creation`() {
        urlProvider.createExecutionUrl(PAYMENT, ATTEMPT_ID, BANK_TRANSFER)
            .apply {
                assertThat(this.toASCIIString())
                    .startsWith("$API_GATEWAY_BASE_URL/example_co/bank_transfer/payu/execute/${PAYMENT.id}/$ATTEMPT_ID")
            }
    }

    @Test
    fun `execution url creation for bank_transfer`() {
        urlProvider.createExecutionUrl(PAYMENT, ATTEMPT_ID, BANK_TRANSFER)
            .apply {
                assertThat(this.toASCIIString())
                    .startsWith("$API_GATEWAY_BASE_URL/example_co/bank_transfer/payu/execute/${PAYMENT.id}/$ATTEMPT_ID")
            }
    }

    @Test
    fun `polling page url creation`() {
        urlProvider.createPollingPageUrl(PAYMENT, ATTEMPT_ID)
            .apply {
                assertThat(this.toASCIIString()).startsWith("$FRONTEND_BASE_URL/co/payment-result")

                val queryParams = queryParams()
                assertThat(queryParams.keys).contains("attemptId")
                assertThat(queryParams["attemptId"]).contains(ATTEMPT_ID)

                assertDefaultQueryParameters()
            }
    }

    @Test
    fun `payment selection page creation`() {
        urlProvider.createPaymentSelectionPageUrl(PAYMENT, RETRY_METHOD)
            .apply {
                assertThat(this.toASCIIString()).startsWith("$FRONTEND_BASE_URL/co")

                val queryParams = queryParams()
                assertThat(queryParams.keys).contains("action")
                assertThat(queryParams["action"]).contains(RETRY_METHOD.name)
            }
    }

    private fun URI.assertDefaultQueryParameters() {
        assertThat(UriComponentsBuilder.fromUri(this).build().queryParams.keys)
            .contains("init", "token")
    }

    private fun URI.queryParams(): MultiValueMap<String, String> =
        UriComponentsBuilder.fromUri(this).build().queryParams
}
