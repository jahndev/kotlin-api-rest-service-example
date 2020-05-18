package com.example.ps.payu.web

import com.example.ps.payu.common.LogContext
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.services.PaymentReturnService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@Suppress("LongParameterList")
class RedirectController(private val logContext: LogContext, private val paymentReturnService: PaymentReturnService) {

    @GetMapping("{integrator}/{method}/payu/redirect/{paymentId}/{attemptId}")
    fun redirect(
        @PathVariable integrator: Integrator,
        @PathVariable method: MethodType,
        @PathVariable paymentId: String,
        @PathVariable attemptId: String,
        @RequestParam("status") payuStatus: PayuStatus,
        response: HttpServletResponse
    ) {
        logContext.updateIntegrator(integrator).updatePaymentId(paymentId)
            .updateMethod(method).updateAttemptId(attemptId)

        PaymentMethodController.logger.info("Handling redirect")

        response.sendRedirect(
            paymentReturnService.handleUserReturn(
                paymentId = paymentId,
                attemptId = attemptId,
                payuStatus = payuStatus
            ).toString()
        )
    }
}
