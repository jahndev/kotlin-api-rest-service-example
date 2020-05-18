package com.example.ps.payu.services

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Status.CAPTURED
import com.example.example.payment.client.dto.Status.PENDING_EXECUTION
import com.example.example.payment.client.dto.Status.REJECTED
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.model.MethodType.BANK_TRANSFER
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuEventType.CHARGE_CREATE
import com.example.ps.payu.model.PayuEventType.CHARGE_UPDATE
import com.example.ps.payu.model.PayuStatus.FAILED
import com.example.ps.payu.model.PayuStatus.PENDING
import com.example.ps.payu.model.PayuStatus.SUCCEED
import com.example.ps.payu.services.exceptions.PayuNotificationException
import com.example.ps.payu.services.helpers.PiiManager
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class PayuNotificationService(
    private val logContext: LogContext,
    private val attemptClient: AttemptClient,
    private val piiManager: PiiManager,
    private val hydraService: HydraService,
    private val paymentClient: PaymentClient
) {

    fun handleNotification(notification: Notification) {
        logContext.updateIntegrator(notification.integrator)
        logContext.updatePaymentId(notification.examplePaymentId)
        logContext.updateAttemptId(notification.attemptId)
        logContext.updateMethod(notification.method)

        logger.info(
            "received ${notification.eventType} notification: " +
                    piiManager.redactPii(notification)
        )

        when (notification.eventType) {
            CHARGE_CREATE.eventType(),
            CHARGE_UPDATE.eventType() ->
                when (notification.status) {
                    SUCCEED -> {
                        attemptClient.update(notification.attemptId, mapOf(STATUS to CAPTURED.name))
                        sendHydraEvent(notification, true)
                    }
                    FAILED -> {
                        attemptClient.update(notification.attemptId, mapOf(STATUS to REJECTED.name))
                        sendHydraEvent(notification, false)
                    }
                    PENDING -> attemptClient.update(notification.attemptId, mapOf(STATUS to PENDING_EXECUTION.name))
                }
            else -> throw PayuNotificationException(
                "invalid eventType ${notification.eventType} for paymentId ${notification.examplePaymentId}"
            )
        }
    }

    private fun sendHydraEvent(notification: Notification, paymentResult: Boolean) {
        val payment = paymentClient.get(notification.examplePaymentId)
        hydraService.sendEvents(
            HydraService.HydraRequest(
                eventName = "payment_execution",
                integrator = notification.integrator.name,
                method = BANK_TRANSFER,
                paymentId = notification.examplePaymentId,
                paymentResult = paymentResult.toString(),
                deviceType = payment.experience.platform,
                attemptId = notification.attemptId
            ).toHydraPayload()
        )
    }

    companion object : KLogging()
}
