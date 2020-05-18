package com.example.ps.payu.services

import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.ps.payu.model.MethodType

interface PaymentInitializationService {

    fun initialize(paymentId: String, methodType: MethodType): ActionResult
}
