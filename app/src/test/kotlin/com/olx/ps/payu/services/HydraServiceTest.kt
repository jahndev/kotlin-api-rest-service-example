package com.example.ps.payu.services

import com.example.example.hydra.client.HydraClient
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClientException

class HydraServiceTest {
    private val hydraClient = spyk<HydraClient>()
    private val hydraService = HydraService(hydraClient)

    @Test fun `send hydra event successful when a HydraTracks is passed by param`() {
        every { hydraClient.sendEvents(any()) } answers { nothing }
        hydraService.sendEvents(getHydraRequest().toHydraPayload())
    }

    @Test fun `do nothing when a RestClientException has been throws by hydraClient `() {
        every { hydraClient.sendEvents(any()) } throws RestClientException("dummy error")
        hydraService.sendEvents(getHydraRequest().toHydraPayload())
    }

    @Test
    fun `get a hydraTracks object valid when a request is created for bank transfer`() {
        val hydraRequest = HydraService.HydraRequest(
            eventName = "payment_execution",
            integrator = Integrator.example_CO.name,
            method = MethodType.BANK_TRANSFER,
            paymentId = PAYMENT_ID,
            attemptId = ATTEMPT_ID,
            paymentResult = "true",
            deviceType = "mobile",
            formValid = "true"
        )
        val hydraTracks = hydraRequest.toHydraPayload()

        hydraTracks.tracks.first().run {
            assert(contains("en=payment_execution"))
            assert(contains("encrypted_payment_id=$PAYMENT_ID"))
            assert(contains("attempt_id=$ATTEMPT_ID"))
            assert(contains("method_type=${MethodType.BANK_TRANSFER.name.toLowerCase()}"))
            assert(contains("psp_name=payu"))
            assert(contains("integrator=example_co"))
            assert(contains("payment_result=true"))
            assert(contains("form_valid=true"))
        }
    }
}

fun getHydraRequest() = HydraService.HydraRequest(
    eventName = "payment_execution",
    integrator = Integrator.example_CO.name,
    method = MethodType.BANK_TRANSFER,
    paymentId = PAYMENT_ID,
    attemptId = ATTEMPT_ID,
    paymentResult = "true",
    deviceType = "mobile",
    formValid = "true"
)