package com.example.ps.payu.services

import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.MethodType

interface PaymentExecutionService {

    fun executePayment(
        paymentId: String,
        attemptId: String,
        methodType: MethodType,
        executionInfo: ExecutionInfo
    ): ActionResult
}
