package com.example.ps.payu.services.impl

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Payment
import com.example.example.payment.client.dto.Status.ERROR
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionStatus
import com.example.example.payment.hateoas.api.v2_1.ActionStatus.WAITING_CUSTOMER_ACTION
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.clients.paymentsos.PaymentsosClient
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.MethodType.BANK_TRANSFER
import com.example.ps.payu.services.HydraService
import com.example.ps.payu.services.toHydraPayload
import com.example.ps.payu.services.PaymentExecutionService
import com.example.ps.payu.services.STATUS
import com.example.ps.payu.services.exceptions.PayuCommunicationException
import com.example.ps.payu.services.helpers.PaymentsosRequestFactory
import mu.KLogging
import org.springframework.stereotype.Service
import java.net.URI

@Service
class PaymentExecutionServiceImpl(
    private val paymentClient: PaymentClient,
    private val attemptClient: AttemptClient,
    private val paymentsosClient: PaymentsosClient,
    private val paymentsosRequestFactory: PaymentsosRequestFactory,
    private val hydraService: HydraService
) : PaymentExecutionService {

    override fun executePayment(
        paymentId: String,
        attemptId: String,
        methodType: MethodType,
        executionInfo: ExecutionInfo
    ): ActionResult {
        val payment = paymentClient.get(paymentId)

        logger.info("Executing attempt for payment: ${payment.id} and attempt: $attemptId")

        val payuPaymentId = paymentsosClient
            .runCatching {
                createPayment(
                    paymentsosRequestFactory.createPaymentRequest(payment),
                    payment.integratorEnum()
                )
            }.getOrElse {
                attemptClient.update(attemptId, mapOf(STATUS to ERROR.name))
                sendHydraEvent(payment, false, attemptId)
                throw PayuCommunicationException(payment, "Error creating payment in payu: ${it.message}", it)
            }

        logger.info { "Created PayU payment with id $payuPaymentId" }

        val redirectionUrl = paymentsosClient
            .runCatching {
                createCharge(
                    payuPaymentId = payuPaymentId,
                    payuChargeRequest = paymentsosRequestFactory.createChargeRequest(
                        payment = payment,
                        attemptId = attemptId,
                        methodType = methodType,
                        executionInfo = executionInfo
                    ),
                    integrator = payment.integratorEnum()
                )
            }.getOrElse {
                attemptClient.update(attemptId, mapOf(STATUS to ERROR.name))
                sendHydraEvent(payment, false, attemptId)
                throw PayuCommunicationException(payment, "Error creating charge", it)
            }

        sendHydraEvent(payment, payuPaymentId.isNotEmpty(), attemptId)
        attemptClient.update(attemptId, mapOf(STATUS to WAITING_CUSTOMER_ACTION.name))
        return createRedirectionResponse(redirectionUrl, WAITING_CUSTOMER_ACTION)
    }

    fun createRedirectionResponse(
        redirectionUrl: String,
        status: ActionStatus
    ) = ActionResult(
        status = status,
        actions = mapOf(
            UserOperation.SUBMIT to Action(
                type = ActionType.REDIRECT,
                httpMethod = HttpMethod.GET,
                uri = URI(redirectionUrl)
            )
        )
    )

    private fun sendHydraEvent(
        payment: Payment,
        formValid: Boolean,
        attemptId: String
    ) {
        hydraService.sendEvents(
            HydraService.HydraRequest(
                eventName = "payment_submitted",
                integrator = payment.integrator,
                method = BANK_TRANSFER,
                paymentId = payment.id,
                deviceType = payment.experience.platform,
                formValid = formValid.toString(),
                attemptId = attemptId
            ).toHydraPayload()
        )
    }

    companion object : KLogging()
}
