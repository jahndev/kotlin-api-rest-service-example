package com.example.ps.payu.clients.paymentsos

import com.ninjasquad.springmockk.MockkBean
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosChargeResponse
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosCreatePaymentResponse
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuOrder
import com.example.ps.payu.clients.paymentsos.dto.PayuProviderData
import com.example.ps.payu.clients.paymentsos.dto.PayuChargeRequest
import com.example.ps.payu.common.PRIVATE_KEY_CO
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.CURRENCY_CO
import com.example.ps.payu.common.APP_ID_CO
import com.example.ps.payu.common.EXTERNAL_PSE_URL
import com.example.ps.payu.common.paymentsosCountryCredential
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.REDIRECTION
import com.example.ps.payu.common.payuChargeRequest
import com.example.ps.payu.common.ADDITIONAL_DETAILS
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.PayuData
import com.example.ps.payu.services.helpers.PaymentsosRequestFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class PaymentsosClientTest {
    private val payuRequestFactory = PaymentsosRequestFactory(mockk(), mockk())

    @MockkBean(relaxed = true)
    private lateinit var configuration: PaymentsosConfiguration

    @MockkBean
    @Qualifier(value = "paymentsosRestTemplate")
    private lateinit var restTemplate: RestTemplate

    @MockkBean(relaxed = true)
    private lateinit var payuCredentialsProvider: PayuCredentialsProvider

    @Autowired
    private lateinit var paymentsosClient: PaymentsosClient

    val paymentRequest = payuRequestFactory.createPaymentRequest(payment = PAYMENT)
    val capturedUrl = slot<String>()
    val capturedHttpEntity = slot<HttpEntity<PaymentsosPaymentRequest>>()
    val payuProviderData = PayuProviderData(
        responseCode = "response",
        description = "description",
        rawResponseJson = null
    )
    val paymentsosChargeResponse = PaymentsosChargeResponse(
        redirection = null,
        result = PayuData.PayuResult(status = "Failed", category = "", subCategory = ""),
        providerSpecificData = PayuData.PayuProviderSpecificData(
            additionalDetails = ADDITIONAL_DETAILS
        ),
        providerData = payuProviderData
    )

    @BeforeEach
    fun setUp() {
        every {
            payuCredentialsProvider.getPaymentsosCredentials(any<String>())
        } returns paymentsosCountryCredential

        every {
            payuCredentialsProvider.getPaymentsosCredentials(any<Integrator>())
        } returns paymentsosCountryCredential
    }

    @Test
    fun `create payment succeeds`() {
        every {
            restTemplate.postForObject(
                capture(capturedUrl),
                capture(capturedHttpEntity),
                any<Class<PaymentsosCreatePaymentResponse>>(),
                *anyVararg()
            )
        } returns PaymentsosCreatePaymentResponse(PAYMENT_ID)

        val actualPaymentId = paymentsosClient.createPayment(paymentRequest, Integrator.example_CO)

        assertThat(actualPaymentId).isEqualTo(PAYMENT_ID)
        assertThat(capturedUrl.captured).isEqualTo("${configuration.baseUrl}/payments")
        with(capturedHttpEntity.captured) {
            assertThat(body).isEqualTo(paymentRequest)
            assertHeaders(this.headers)
        }
    }

    @Test
    fun `failing create payment requests are retried`() {
        val request = payuRequestFactory.createPaymentRequest(
            payment = PAYMENT
        )

        every {
            restTemplate.postForObject(
                any(),
                any<HttpEntity<PaymentsosPaymentRequest>>(),
                any<Class<PaymentsosCreatePaymentResponse>>(),
                *anyVararg()
            )
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)

        assertThrows<HttpServerErrorException> {
            paymentsosClient.createPayment(request, Integrator.example_CO)
        }

        verify(exactly = 3) {
            restTemplate.postForObject(
                any(),
                any<HttpEntity<PaymentsosPaymentRequest>>(),
                any<Class<PaymentsosCreatePaymentResponse>>(),
                *anyVararg()
            )
        }
    }

    @Test
    fun `createPayment throws exception when payU response is null`() {
        val payuPaymentRequest = PaymentsosPaymentRequest(
            amount = 10,
            currency = CURRENCY_CO,
            order = PayuOrder("", emptyList()),
            statementSoftDescriptor = PAYMENT.order.description,
            billingAddress = null,
            shippingAddress = null
        )

        every {
            restTemplate.postForObject(
                any(), any(), any<Class<PaymentsosCreatePaymentResponse>>(), *anyVararg()
            )
        } returns null

        assertThrows<HttpServerErrorException> { paymentsosClient.createPayment(payuPaymentRequest, Integrator.example_CO) }
    }

    @Test
    fun `createCharge returns url of new created charge`() {
        val url = slot<String>()
        val httpEntity = slot<HttpEntity<PaymentsosPaymentRequest>>()
        every {
            restTemplate.postForObject(
                capture(url), capture(httpEntity), any<Class<PaymentsosChargeResponse>>(), *anyVararg()
            )
        } returns PaymentsosChargeResponse(REDIRECTION, mockk(), mockk(), mockk())

        val pseExternalUrl = paymentsosClient.createCharge(PAYMENT_ID, payuChargeRequest, Integrator.example_CO)

        assertThat(pseExternalUrl).isEqualTo(EXTERNAL_PSE_URL.toASCIIString())
        assertThat(url.captured).isEqualTo("${configuration.baseUrl}/payments/$PAYMENT_ID/charges")
        with(httpEntity.captured) {
            assertThat(body).isEqualTo(payuChargeRequest)
            assertHeaders(this.headers)
            assertThat(headers[IDEMPOTENCY_KEY]).contains(PAYMENT_ID)
        }
    }

    @Test
    fun `createCharge throws exception when payU response is null`() {
        every {
            restTemplate.postForObject<PaymentsosChargeResponse?>(any(), any<Class<PayuChargeRequest>>(), *anyVararg())
        } returns null

        assertThrows<HttpServerErrorException> {
            paymentsosClient.createCharge(PAYMENT_ID, payuChargeRequest, Integrator.example_CO)
        }
    }

    fun assertHeaders(headers: HttpHeaders) {
        assertThat(headers[APP_ID]).contains(APP_ID_CO)
        assertThat(headers[PRIVATE_KEY]).contains(PRIVATE_KEY_CO)
        assertThat(headers[CONTENT_TYPE]).contains(APPLICATION_JSON_VALUE)
        assertThat(headers[API_VERSION]).contains(configuration.apiVersion)
        assertThat(headers[X_PAYMENTS_OS_ENV]).contains("test")
    }

    @Test
    fun `thrown httpServerErrorException when post to create charge fails and redirection is null`() {
        every {
            restTemplate.postForObject(
                capture(capturedUrl),
                capture(capturedHttpEntity),
                any<Class<PaymentsosChargeResponse>>(),
                *anyVararg()
            )
        } returns paymentsosChargeResponse

        val httpServerErrorException = assertThrows<HttpServerErrorException> {
            paymentsosClient.createCharge(PAYMENT_ID, payuChargeRequest, Integrator.example_CO)
        }

        val expected = "500 Can not create paymentsos charge resource for payuPaymentId: '$PAYMENT_ID', " +
                "code: '${paymentsosChargeResponse.providerData?.responseCode}', " +
                "description: '${paymentsosChargeResponse.providerData?.description}', " +
                "message: null, " + paymentsosChargeResponse.providerSpecificData

        assertThat(httpServerErrorException.message).isEqualTo(expected)
    }

    fun createHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add("app_id", "")
            add("private_key", "")
            add("Content-Type", "application/json")
            add("api-version", "")
            add("x-payments-os-env", "")
            add("idempotency_key", "1")
            add("x-client-ip-address", "")
        }

    @Test
    fun `given a valid raw_response then the method toBeautyString return a valid string`() {
        val payuProviderData = payuProviderData.copy(
            rawResponseJson = "{\"responseMessage\":\"MESSAGE\"}"
        )

        val expected = "code: '${paymentsosChargeResponse.providerData?.responseCode}', " +
                "description: '${paymentsosChargeResponse.providerData?.description}', " +
                "message: MESSAGE"

        paymentsosClient.run {
            assertThat(payuProviderData.toBeautyString()).isEqualTo(expected)
        }
    }
}
