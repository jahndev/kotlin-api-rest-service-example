package com.example.ps.payu.services.impl

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Status.CAPTURED
import com.example.example.payment.client.dto.Status.ERROR
import com.example.example.payment.client.dto.Status.PENDING_EXECUTION
import com.example.example.payment.client.dto.Status.REJECTED
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.model.PayuStatus.FAILED
import com.example.ps.payu.model.PayuStatus.PENDING
import com.example.ps.payu.model.PayuStatus.SUCCEED
import com.example.ps.payu.services.PaymentReturnService
import com.example.ps.payu.services.STATUS
import com.example.ps.payu.services.helpers.UrlProvider
import mu.KLogging
import org.springframework.stereotype.Service
import java.net.URI

@Service
class PaymentReturnServiceImpl(
    private val paymentClient: PaymentClient,
    private val attemptClient: AttemptClient,
    private val urlProvider: UrlProvider
) : PaymentReturnService {
    override fun handleUserReturn(
        paymentId: String,
        attemptId: String,
        payuStatus: PayuStatus
    ): URI {
        val payment = paymentClient.get(paymentId)
        val attempts = attemptClient.getAttempts(paymentId, listOf(CAPTURED, ERROR, REJECTED))
        val paymentSelectionUrl = urlProvider.createPaymentSelectionPageUrl(payment)
        return when {
            attempts.any { it.status == CAPTURED } -> URI(payment.redirectUrls.experienceCompleted)
            attempts.filter { it.id == attemptId }
                .any { it.status in setOf(ERROR, REJECTED) } -> paymentSelectionUrl
            payuStatus == SUCCEED -> {
                attemptClient.update(
                    attemptId = attemptId,
                    patch = mapOf(STATUS to CAPTURED.name)
                )
                URI(payment.redirectUrls.experienceCompleted)
            }
            payuStatus == PENDING -> {
                attemptClient.update(
                    attemptId = attemptId,
                    patch = mapOf(STATUS to PENDING_EXECUTION.name)
                )
                urlProvider.createPollingPageUrl(payment, attemptId)
            }
            payuStatus == FAILED -> {
                attemptClient.update(
                    attemptId = attemptId,
                    patch = mapOf(STATUS to ERROR.name)
                )
                paymentSelectionUrl
            }
            else -> paymentSelectionUrl
        }
    }

    companion object : KLogging()
}
