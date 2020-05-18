package com.example.ps.payu.web

import com.example.ps.payu.common.SIGNATURE_MISMATCH
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuData
import com.example.ps.payu.model.PayuNotification
import com.example.ps.payu.services.PSP_PAYU
import com.example.ps.payu.services.PayuNotificationService
import com.example.ps.payu.services.helpers.PayuSignatureService
import com.example.ps.payu.services.helpers.PiiManager
import com.example.ps.payu.web.converters.toPayuNotificationHeaders
import com.example.ps.payu.web.requests.PayuNotificationRequest
import com.example.ps.payu.web.requests.PayuNotificationHeaders
import com.example.ps.payu.web.requests.status
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class NotificationController(
    private val notificationService: PayuNotificationService,
    private val payuSignatureService: PayuSignatureService,
    private val piiManager: PiiManager
) {
    @PostMapping("{integrator}/payu/notifications")
    fun handleNotification(
        @PathVariable integrator: Integrator,
        @RequestBody payuNotification: PayuNotificationRequest,
        @RequestHeader headers: HttpHeaders
    ) {
        val additionalDetails = payuNotification.data.providerSpecificData.additionalDetails

        val payuNotificationHeaders = headers.toPayuNotificationHeaders()

        val notification = Notification(
            eventType = payuNotificationHeaders.eventType,
            examplePaymentId = additionalDetails.examplePaymentId,
            attemptId = payuNotification.data.reconciliationId,
            method = MethodType.BANK_TRANSFER,
            status = payuNotification.status,
            integrator = integrator,
            psp = PSP_PAYU,
            payuNotification = toPayuNotification(payuNotification)
        )

        validateSignature(notification, payuNotificationHeaders, payuNotification)

        notificationService.handleNotification(notification)
    }

    private fun validateSignature(
        notification: Notification,
        payuNotificationHeaders: PayuNotificationHeaders,
        payuNotificationRequest: PayuNotificationRequest
    ) {
        val validSignature = payuSignatureService.verifySignature(
            notification,
            payuNotificationHeaders.eventType,
            payuNotificationHeaders.signature
        )
        if (!validSignature) {
            logger.error(
                "Could not verify notification signature, notification: " +
                "${piiManager.redactPii(notification)}, " +
                "headers: ${piiManager.redactPii(payuNotificationHeaders)} " +
                "request: ${piiManager.redactPii(payuNotificationRequest)} "
            )
            throw IllegalArgumentException(SIGNATURE_MISMATCH)
        }
    }

    private fun toPayuNotification(payuNotification: PayuNotificationRequest) =
        PayuNotification(
            id = payuNotification.id,
            created = payuNotification.created,
            paymentId = payuNotification.paymentId,
            accountId = payuNotification.accountId,
            appId = payuNotification.appId,
            data = PayuData(
                id = payuNotification.data.id,
                result = PayuData.PayuResult(
                    payuNotification.data.result.status,
                    payuNotification.data.result.category,
                    payuNotification.data.result.subCategory
                ),
                providerSpecificData = PayuData.PayuProviderSpecificData(
                    payuNotification.data.providerSpecificData.additionalDetails
                ),
                reconciliationId = payuNotification.data.reconciliationId,
                providerData = PayuData.PayuProviderData(
                    responseCode = payuNotification.data.providerData.responseCode
                ),
                amount = payuNotification.data.amount,
                currency = payuNotification.data.currency
            )
        )

    companion object : KLogging()
}
