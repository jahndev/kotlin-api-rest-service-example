package com.example.ps.payu.services.impl

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Attempt
import com.example.example.payment.client.dto.Payment
import com.example.example.payment.client.dto.Status.CREATED
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionStatus
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.ContentType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.PaymentListPageContext
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.BANK_CODE
import com.example.ps.payu.services.BANK_NAME
import com.example.ps.payu.services.ISSUER_ID
import com.example.ps.payu.services.LOGO_URL
import com.example.ps.payu.services.NAME
import com.example.ps.payu.services.ONLINE
import com.example.ps.payu.services.PSP
import com.example.ps.payu.services.PSP_PAYU
import com.example.ps.payu.services.PaymentInitializationService
import com.example.ps.payu.services.SUBTYPE_EMPTY
import com.example.ps.payu.services.exceptions.NotSupportedPaymentException
import com.example.ps.payu.services.helpers.PaymentMethodValidator
import com.example.ps.payu.services.helpers.PayuBankService
import com.example.ps.payu.services.helpers.UrlProvider
import mu.KLogging
import org.springframework.stereotype.Service
import java.net.URI

@Service
class PaymentInitializationServiceImpl(
    private val paymentClient: PaymentClient,
    private val attemptClient: AttemptClient,
    private val payuBankService: PayuBankService,
    private val urlProvider: UrlProvider,
    private val paymentMethodValidator: PaymentMethodValidator,
    private val logContext: LogContext
) : PaymentInitializationService {

    override fun initialize(paymentId: String, methodType: MethodType): ActionResult {
        logger.info("Initializing payment")

        val payment = paymentClient.get(paymentId)

        if (!paymentMethodValidator.isPaymentSupported(methodType, payment)) {
            throw NotSupportedPaymentException(payment, "payment is not supported for methodType: $methodType")
        }

        val attemptId = attemptClient.create(
            Attempt(
                paymentId = paymentId,
                methodType = methodType.name,
                subtype = SUBTYPE_EMPTY,
                status = CREATED,
                psp = PSP_PAYU
            )
        )

        logContext.updateAttemptId(attemptId)

        return with(payment) {
            ActionResult(
                status = ActionStatus.CREATED,
                options = getActionList(this, attemptId, methodType),
                context = PaymentListPageContext(
                    description = order.description,
                    grossValue = amount.grossValue,
                    currency = amount.currency.currencyCode,
                    cancelUrl = redirectUrls.experienceCancelled
                )
            )
        }
    }

    private fun getActionList(payment: Payment, attemptId: String, methodType: MethodType): List<Action> {
        val url = urlProvider.createExecutionUrl(payment, attemptId, methodType)

        return payuBankService.getBankList(payment.integratorEnum())
            .map { createAction(url, it.pseCode, it.description) }
    }

    fun createAction(
        executionUri: URI,
        bankCode: String,
        bankName: String
    ) = Action(
        type = ActionType.API,
        httpMethod = HttpMethod.POST,
        contentType = ContentType.JSON,
        uri = executionUri,
        params = mapOf(
            ISSUER_ID to BANK_TRANSFER_VENDOR_PSE,
            BANK_CODE to bankCode
        ),
        context = mapOf(
            NAME to MethodType.BANK_TRANSFER.name,
            LOGO_URL to "",
            PSP to PSP_PAYU,
            ONLINE to "",
            BANK_NAME to bankName
        )
    )

    companion object : KLogging()
}
