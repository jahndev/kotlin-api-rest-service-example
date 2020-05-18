package com.example.ps.payu.services.helpers

import com.example.example.authentication.client.AuthenticationClient
import com.example.example.authentication.client.dto.Claims
import com.example.example.authentication.client.dto.JwtGenerationPayload
import com.example.example.payment.client.dto.Payment
import com.example.example.payment.hateoas.api.v2_1.UserMessage
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.common.toInitParameter
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val ISSUER = "example"
private const val TOKEN_VALIDITY_DURATION = "PT20M"
private const val PAYU = "payu"

@Component
class UrlProvider(
    private val authenticationClient: AuthenticationClient,
    @Value("\${apiGatewayBaseUrl}") private val apiGatewayBaseUrl: String,
    @Value("\${frontendBaseUrl}") private val frontendBaseUrl: String
) {
    fun createInitializationPageUrl(paymentInfo: Payment, method: MethodType): URI =
        UriComponentsBuilder
            .fromPath("${paymentInfo.integratorEnum().path}/${method.code}")
            .queryParam("act", "initialize")
            .queryParam("psp", PAYU)
            .addDefaultQueryParameters(paymentInfo)
            .build()
            .toUri()

    private fun UriComponentsBuilder.addDefaultQueryParameters(payment: Payment) =
        this.queryParam("init", payment.toInitParameter())
            .queryParam(
                "token",
                authenticationClient.generateToken(
                    JwtGenerationPayload(
                        claims = Claims(payment.id),
                        expirationTime = TOKEN_VALIDITY_DURATION,
                        issuer = ISSUER
                    )
                ).token
            )

    fun createExecutionUrl(payment: Payment, attemptId: String, method: MethodType): URI =
        with(payment) {
            UriComponentsBuilder.fromHttpUrl(
                "$apiGatewayBaseUrl/${integrator.toLowerCase()}/${method.code}/$PAYU/execute/$id/$attemptId"
            )
                .build()
                .toUri()
        }

    fun createRedirectUrl(
        integrator: Integrator,
        paymentId: String,
        attemptId: String,
        methodType: MethodType
    ): URI =
        URI.create(buildString {
            append("$apiGatewayBaseUrl/${integrator.name.toLowerCase()}/${methodType.code}/$PAYU")
            append("/redirect")
            append("/$paymentId/$attemptId")
        })

    private fun String.countryCode() = Integrator.valueOf(this).country.countryCode.toLowerCase()

    fun createPaymentSelectionPageUrl(
        payment: Payment,
        action: UserMessage = UserMessage.UNKNOWN
    ): URI = UriComponentsBuilder
        .fromHttpUrl("$frontendBaseUrl/${payment.integrator.countryCode()}")
        .queryParam("action", action.name)
        .addDefaultQueryParameters(payment)
        .build()
        .toUri()

    fun createPollingPageUrl(
        payment: Payment,
        attemptId: String
    ): URI =
        UriComponentsBuilder
            .fromHttpUrl("$frontendBaseUrl/${payment.integrator.countryCode()}/payment-result")
            .queryParam("attemptId", attemptId)
            .addDefaultQueryParameters(payment)
            .build()
            .toUri()
}
