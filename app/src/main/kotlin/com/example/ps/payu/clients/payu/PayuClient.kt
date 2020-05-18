package com.example.ps.payu.clients.payu

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.clients.payu.dto.PayuBanksRequest
import com.example.ps.payu.clients.payu.dto.PayuBanksResponse
import com.example.ps.payu.services.helpers.PiiManager
import mu.KLogging
import org.apache.http.HttpHeaders.ACCEPT
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.EMPTY
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import kotlin.streams.toList
import kotlin.text.Charsets.UTF_8

@Component
class PayuClient(
    @Qualifier(value = "payuRestTemplate")
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val piiManager: PiiManager,
    configuration: PayuConfiguration
) {
    private val bankListURL = "${configuration.url}/${configuration.version}/service.cgi"

    /**
     * Creates payment and returns id of a new created payment
     */
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 0), include = [HttpServerErrorException::class,
        ResourceAccessException::class])
    fun getBanks(request: PayuBanksRequest): List<PayuBank> {
        PayuClient.logger.info("getting bank list: ${piiManager.redactPii(request)}")

        val payuBanksResponse = restTemplate.postForObject(
            bankListURL,
            HttpEntity(request, getRequestHeaders()),
            PayuBanksResponse::class.java
        )

        return payuBanksResponse?.banks?.stream()?.filter {
            payuBank -> payuBank?.pseCode != "0"
        }?.toList() ?: throw HttpServerErrorException.create(
            INTERNAL_SERVER_ERROR, "Cannot get bank list from request: '$request', " +
                    "the response was '${payuBanksResponse?.code}' ${payuBanksResponse?.error}",
            EMPTY,
            objectMapper.writeValueAsBytes(payuBanksResponse),
            UTF_8
        )
    }

    private fun getRequestHeaders() = HttpHeaders()
        .apply {
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            set(ACCEPT, APPLICATION_JSON_VALUE)
        }

    companion object : KLogging()
}