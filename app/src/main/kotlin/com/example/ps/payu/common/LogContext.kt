package com.example.ps.payu.common

import com.newrelic.api.agent.NewRelic
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import org.slf4j.MDC
import org.springframework.stereotype.Component

const val MDC_PAYMENT_ID = "paymentId"
const val MDC_ATTEMPT_ID = "attemptId"
const val MDC_INTEGRATOR = "integrator"
const val MDC_METHOD_TYPE = "methodType"
const val MDC_CORRELATION_ID = "correlationId"

@Component
class LogContext {

    val correlationId: String?
        get() = MDC.get(MDC_CORRELATION_ID)

    fun updateCorrelationId(correlationId: String): LogContext {
        MDC.put(MDC_CORRELATION_ID, correlationId)
        NewRelic.addCustomParameter(MDC_CORRELATION_ID, correlationId)
        NewRelic.addCustomParameter(HEADER_NAME_CORRELATION_ID, correlationId) // for consistency reasons
        return this
    }

    fun updateIntegrator(integrator: Integrator): LogContext {
        MDC.put(MDC_INTEGRATOR, integrator.name)
        NewRelic.addCustomParameter(MDC_INTEGRATOR, integrator.name)
        return this
    }

    fun updateMethod(method: MethodType): LogContext {
        MDC.put(MDC_METHOD_TYPE, method.name)
        NewRelic.addCustomParameter(MDC_METHOD_TYPE, method.name)
        return this
    }

    fun updatePaymentId(paymentId: String): LogContext {
        MDC.put(MDC_PAYMENT_ID, paymentId)
        NewRelic.addCustomParameter(MDC_PAYMENT_ID, paymentId)
        return this
    }

    fun updateAttemptId(attemptId: String): LogContext {
        MDC.put(MDC_ATTEMPT_ID, attemptId)
        NewRelic.addCustomParameter(MDC_ATTEMPT_ID, attemptId)
        return this
    }

    fun clear() = MDC.clear()
}