package com.example.ps.payu.clients.paymentsos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosCreatePaymentResponse
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuChargeRequest
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosChargeResponse
import com.example.ps.payu.clients.paymentsos.dto.PayuProviderData
import com.example.ps.payu.clients.paymentsos.dto.PayuRawResponse
import com.example.ps.payu.model.Integrator
import mu.KLogging
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject
import java.util.UUID
import kotlin.text.Charsets.UTF_8

@Component
class PaymentsosClient(
    @Qualifier(value = "paymentsosRestTemplate")
    private val restTemplate: RestTemplate,
    private val configuration: PaymentsosConfiguration,
    private val payuCredentialsProvider: PayuCredentialsProvider,
    private val objectMapper: ObjectMapper
) {
    val createPaymentPath = "${configuration.baseUrl}/payments"

    /**
     * Creates payment and returns id of a new created payment
     */
    @Retryable(maxAttempts = 3, include = [HttpServerErrorException::class, ResourceAccessException::class])
    fun createPayment(request: PaymentsosPaymentRequest, integrator: Integrator): String {
        logger.info("creating payu payment: $request")
        val paymentsosCreatePaymentResponse = restTemplate.postForObject(
            createPaymentPath,
            HttpEntity(request, getRequestHeaders(configuration, integrator)),
            PaymentsosCreatePaymentResponse::class.java
        )

        return paymentsosCreatePaymentResponse?.id ?: throw HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Can not create payment for request: '$request', " +
                "the response was '$paymentsosCreatePaymentResponse'",
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(paymentsosCreatePaymentResponse),
            UTF_8
        )
    }

    /**
     * Returns redirection url
     */
    @Retryable(maxAttempts = 3, include = [HttpServerErrorException::class, ResourceAccessException::class])
    fun createCharge(
        payuPaymentId: String,
        payuChargeRequest: PayuChargeRequest,
        integrator: Integrator
    ): String {
        logger.info("Creating payu charge request: $payuChargeRequest, payuPaymentId: $payuPaymentId")
        val createChargePath = "${configuration.baseUrl}/payments/$payuPaymentId/charges"

        val headers = getRequestHeaders(configuration, integrator, payuPaymentId)
            .apply {
                set(X_CLIENT_IP_ADDRESS, configuration.clientIp)
            }

        val paymentsosChargeResponse = restTemplate
            .postForObject<PaymentsosChargeResponse?>(createChargePath, HttpEntity(payuChargeRequest, headers))

        return paymentsosChargeResponse?.redirection?.url?.toString() ?: throw HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Can not create paymentsos charge resource for payuPaymentId: '$payuPaymentId', " +
                    paymentsosChargeResponse?.providerData?.toBeautyString() + ", " +
                    paymentsosChargeResponse?.providerSpecificData,
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(paymentsosChargeResponse),
            UTF_8
        )
    }

    fun PayuProviderData.toBeautyString(): String =
        "code: '$responseCode', description: '$description', message: ${rawResponseJson?.let {
            objectMapper.readValue<PayuRawResponse>(it) }?.responseMessage}"

    private fun getRequestHeaders(
        paymentsosConfiguration: PaymentsosConfiguration,
        integrator: Integrator,
        idemPotencyKey: String = UUID.randomUUID().toString()
    ) = HttpHeaders().apply {
        with(payuCredentialsProvider.getPaymentsosCredentials(integrator)) {
            set(APP_ID, appId)
            set(PRIVATE_KEY, privateKey)
            set(X_PAYMENTS_OS_ENV, environment)
        }
        set(API_VERSION, paymentsosConfiguration.apiVersion)
        set(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        set(IDEMPOTENCY_KEY, idemPotencyKey)
    }

    companion object : KLogging()
}
