package com.example.ps.payu.services

import com.newrelic.api.agent.NewRelic
import com.example.example.hydra.client.HydraClient
import com.example.example.hydra.client.dto.HydraTracks
import com.example.ps.payu.model.MethodType
import mu.KLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException

@Service
class HydraService(private val hydraClient: HydraClient) {

    data class HydraRequest(
        val eventName: String,
        val integrator: String,
        val method: MethodType,
        val paymentId: String,
        val attemptId: String = "",
        val paymentResult: String? = "",
        val deviceType: String? = "",
        val formValid: String? = ""
    )

    @Async
    fun sendEvents(tracks: HydraTracks) {
        logger.info("send Hydra events")
        try {
            hydraClient.sendEvents(tracks)
        } catch (e: RestClientException) {
            logger.error("Exception when sending tracks to Hydra", e)
            NewRelic.noticeError("Sending Hydra Event failed")
        }
    }

    companion object : KLogging()
}

fun HydraService.HydraRequest.toHydraPayload() =
    HydraTracks(listOf(buildString {
        append("&en=$eventName")
        append("&encrypted_payment_id=$paymentId")
        append("&back_end_attempt_id=$attemptId")
        append("&front_end_attempt_id=$attemptId")
        append("&method_type=${method.name.toLowerCase()}")
        append("&psp_name=$PSP_PAYU")
        append("&integrator=${integrator.toLowerCase()}")
        append("&payment_result=$paymentResult")
        append("&form_valid=$formValid")
        append("&device_type=${deviceType?.toLowerCase()}")
    }))
