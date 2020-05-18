package com.example.ps.payu.web

import com.example.example.payment.client.dto.Payment
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.PaymentMethod
import com.example.ps.payu.services.PaymentExecutionService
import com.example.ps.payu.services.PaymentInitializationService
import com.example.ps.payu.services.PaymentMethodService
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@SuppressWarnings("UnusedPrivateMember")
class PaymentMethodController(
    private val paymentMethodService: PaymentMethodService,
    private val paymentInitializationService: PaymentInitializationService,
    private val logContext: LogContext,
    private val paymentExecutionService: PaymentExecutionService
) {

    @PostMapping("{integrator}/payu/methods")
    fun getAvailablePaymentMethods(
        @PathVariable integrator: Integrator,
        @RequestBody payment: Payment
    ): Collection<PaymentMethod> {
        logContext.updateIntegrator(integrator).updatePaymentId(payment.id)
        return if (payment.integrator == integrator.name) {
            paymentMethodService.getAvailablePaymentMethods(payment).also {
                logger.info("Found ${it.size} available payment methods")
            }
        } else {
            logger.warn(
                "Provided integrator: $integrator " +
                    "doesn't match with payment integrator: ${payment.integrator}"
            )
            emptyList()
        }
    }

    @GetMapping("{integrator}/{method}/payu/initialize/{paymentId}")
    fun initialize(
        @PathVariable integrator: Integrator,
        @PathVariable method: MethodType,
        @PathVariable paymentId: String
    ): ActionResult {
        logContext.updateIntegrator(integrator).updatePaymentId(paymentId).updateMethod(method)
        return paymentInitializationService.initialize(paymentId, method)
    }

    @PostMapping("{integrator}/{method}/payu/execute/{paymentId}/{attemptId}")
    fun execute(
        @PathVariable integrator: Integrator,
        @PathVariable method: MethodType,
        @PathVariable paymentId: String,
        @PathVariable attemptId: String,
        @RequestBody executionInfo: ExecutionInfo
    ): ActionResult {
        logContext.updateIntegrator(integrator).updatePaymentId(paymentId).updateMethod(method)
            .updateAttemptId(attemptId)

        return paymentExecutionService.executePayment(
            paymentId = paymentId,
            attemptId = attemptId,
            methodType = method,
            executionInfo = executionInfo
        )
    }

    companion object : KLogging()
}
