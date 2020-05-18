package com.example.ps.payu.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Status.CAPTURED
import com.example.example.payment.client.dto.Status.PENDING_EXECUTION
import com.example.example.payment.client.dto.Status.REJECTED
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.example_PAYMENT_ID
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.getPayuNotification
import com.example.ps.payu.model.Integrator.example_CO
import com.example.ps.payu.model.MethodType.BANK_TRANSFER
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.services.exceptions.PayuNotificationException
import com.example.ps.payu.services.helpers.PiiManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PayuNotificationServiceTest {

    private val attemptClient = mockk<AttemptClient>()

    private val hydraService = mockk<HydraService>()

    private val paymentClient = mockk<PaymentClient>()

    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    private val logContext = spyk(LogContext())

    private val notificationService: PayuNotificationService =
        PayuNotificationService(
            logContext = logContext,
            attemptClient = attemptClient,
            paymentClient = paymentClient,
            piiManager = PiiManager(objectMapper),
            hydraService = hydraService
        )

    @BeforeEach
    fun setUp() {
        every { hydraService.sendEvents(any()) } answers { nothing }
        every { paymentClient.get("myPaymentId") } answers { PAYMENT }
    }

    @Test
    fun `handle CHARGE_UPDATE successful notification`() {
        val notification = getNotification("payment.charge.update", "Succeed").copy(
            attemptId = "myAttemptId",
            examplePaymentId = "myPaymentId"
        )

        every {
            attemptClient.getAttempts(notification.examplePaymentId, listOf(CAPTURED, PENDING_EXECUTION))
        } returns emptyList()

        every {
            attemptClient.update(attemptId = "myAttemptId", patch = mapOf("status" to CAPTURED.name))
        } answers { nothing }

        notificationService.handleNotification(notification)

        verify {
            logContext.updateAttemptId("myAttemptId")
            logContext.updatePaymentId("myPaymentId")
            logContext.updateIntegrator(example_CO)
            logContext.updateMethod(BANK_TRANSFER)
        }

        verify {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf(STATUS to CAPTURED.name)
            )
        }

        verify {
            hydraService.sendEvents(
                HydraService.HydraRequest(
                    eventName = "payment_execution",
                    integrator = notification.integrator.name,
                    method = BANK_TRANSFER,
                    paymentId = notification.examplePaymentId,
                    paymentResult = "true",
                    deviceType = PAYMENT.experience.platform,
                    attemptId = notification.attemptId
                ).toHydraPayload()
            )
        }
    }

    @Test
    fun `handle Charge Create successful notification`() {
        val notification = getNotification("payment.charge.create", "Succeed").copy(
            attemptId = "myAttemptId",
            examplePaymentId = "myPaymentId"
        )

        every {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf("status" to CAPTURED.name)
            )
        } answers { nothing }

        notificationService.handleNotification(notification)

        verify {
            logContext.updateAttemptId("myAttemptId")
            logContext.updatePaymentId("myPaymentId")
            logContext.updateIntegrator(example_CO)
            logContext.updateMethod(BANK_TRANSFER)
        }

        verify {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf("status" to CAPTURED.name)
            )
        }
    }

    @Test
    fun `handle Charge Create failed notification`() {
        val notification = getNotification("payment.charge.create", "Failed").copy(
            attemptId = "myAttemptId",
            examplePaymentId = "myPaymentId"
        )

        every {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf("status" to REJECTED.name)
            )
        } answers { nothing }

        notificationService.handleNotification(notification)

        verify {
            logContext.updateAttemptId("myAttemptId")
            logContext.updatePaymentId("myPaymentId")
            logContext.updateIntegrator(example_CO)
            logContext.updateMethod(BANK_TRANSFER)
        }

        verify {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf("status" to REJECTED.name)
            )
        }

        verify {
            hydraService.sendEvents(
                HydraService.HydraRequest(
                    eventName = "payment_execution",
                    integrator = notification.integrator.name,
                    method = BANK_TRANSFER,
                    paymentId = notification.examplePaymentId,
                    paymentResult = "false",
                    deviceType = PAYMENT.experience.platform,
                    attemptId = notification.attemptId
                ).toHydraPayload()
            )
        }
    }

    @Test
    fun `a PayuNotificationException when invalid eventype`() {
        val notification = getNotification("payment.charge.unknown", "Succeed").copy(
            eventType = "unknown"
        )

        val message = "invalid eventType ${notification.eventType} for paymentId ${notification.examplePaymentId}"
        val cause = PayuNotificationException(message)

        every {
            attemptClient.getAttempts(
                notification.examplePaymentId, listOf(CAPTURED, PENDING_EXECUTION)
            )
        } returns emptyList()

        every {
            attemptClient.update(
                attemptId = "myAttemptId",
                patch = mapOf("status" to CAPTURED.name)
            )
        } answers { nothing }

        val exception = assertThrows<PayuNotificationException> {
            notificationService.handleNotification(notification)
        }

        assertThat(exception.message).isEqualTo(cause.message)
    }

    fun getNotification(eventType: String, status: String) = Notification(
        eventType = eventType,
        examplePaymentId = example_PAYMENT_ID,
        attemptId = ATTEMPT_ID,
        method = BANK_TRANSFER,
        status = PayuStatus.valueOf(status.toUpperCase()),
        integrator = example_CO,
        psp = PSP_PAYU,
        payuNotification = getPayuNotification(status)
    )
}