package com.example.ps.payu.web

import com.ninjasquad.springmockk.MockkBean
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.services.PSP_PAYU
import com.example.ps.payu.services.PayuNotificationService
import com.example.ps.payu.services.helpers.PayuSignatureService
import com.example.ps.payu.services.helpers.PiiManager
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX

@ExtendWith(SpringExtension::class)
@RunWith(SpringRunner::class)
@WebMvcTest(value = [NotificationController::class], excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean(relaxed = true)
    private lateinit var payuSignatureService: PayuSignatureService

    @MockkBean(relaxed = true)
    private lateinit var notificationService: PayuNotificationService

    @MockkBean
    private lateinit var piiManager: PiiManager

    @Suppress("UnusedPrivateMember")
    @MockkBean(relaxed = true)
    private lateinit var logContext: LogContext

    @Suppress("UnusedPrivateMember")
    @MockkBean
    private lateinit var urlProvider: UrlProvider

    private val slot = slot<Notification>()
    private val dtoSlot = slot<Notification>()
    private val eventTypeSlot = slot<String>()
    private val signatureSlot = slot<String>()

    @Test
    fun `receive authorization update notification`() {
        every { payuSignatureService.verifySignature(any(), any(), any()) } returns true

        val eventType = "payment.charge.update"
        val signature = "sig1=d569c0a3e8aa62ad35c70ab3bacae484ebc5af10f65bc5b5d59cc5fd295c262d"
        sendNotification(eventType = eventType, signature = signature, status = HttpStatus.OK)

        verify { notificationService.handleNotification(capture(slot)) }
        verify {
            payuSignatureService.verifySignature(capture(dtoSlot), capture(eventTypeSlot), capture(signatureSlot))
        }

        val notification = slot.captured

        assertThat(notification.integrator).isEqualTo(Integrator.example_CO)
        assertThat(notification.psp).isEqualTo(PSP_PAYU)
        assertThat(signatureSlot.captured).isEqualTo(signature)
        assertThat(eventTypeSlot.captured).isEqualTo(eventType)
        assertThat(notification.payuNotification.id).isEqualTo("a6e1647c-2b66-4559-b9b9-21cc15268c4c-2020-01-14T06:" +
                "25:38.154Z-b1f04ff0-351e-4a51-a4cd-bfa5e77f51f9")
        assertThat(notification.examplePaymentId).isEqualTo("9037b5e992b6f062c069584f655ad2db65439b15")
        assertThat(notification.eventType).isEqualTo(eventType)
        assertThat(notification.payuNotification.data.reconciliationId)
            .isEqualTo("9bc9921555ca12418fc70ad6f356d33b692f41dc")
        assertThat(notification.method).isEqualTo(MethodType.BANK_TRANSFER)
        assertThat(notification.status).isEqualTo(PayuStatus.PENDING)
    }

    @Test
    fun `handle invalid signature`() {
        every { payuSignatureService.verifySignature(any(), any(), any()) } returns false
        every { piiManager.redactPii(any()) } returns ""

        val result = sendNotification("", "", HttpStatus.BAD_REQUEST)
        result.andExpect(MockMvcResultMatchers.jsonPath("\$.errorMessage").exists())
    }

    private fun sendNotification(eventType: String, signature: String, status: HttpStatus): ResultActions {
        val payuVersion = "1.2.0"
        val env = "test"
        val zoozRequestId = "eb8b67e8-c2ef-4158-b4cb-db5a1473ad87"

        return mockMvc.perform(
            post("/example_co/payu/notifications")
                .contentType(APPLICATION_JSON_UTF8)
                .content(loadNotification("webhook_pending_status.json"))
                .header("Signature", signature)
                .header("Event-Type", eventType)
                .header("Version", payuVersion)
                .header("X-Payments-Os-Env", env)
                .header("X-Zooz-Request-Id", zoozRequestId)
        )
            .andExpect(status().`is`(status.value()))
    }

    private fun loadNotification(filename: String) =
        ResourceUtils.getFile("$CLASSPATH_URL_PREFIX$filename").readText()
}
