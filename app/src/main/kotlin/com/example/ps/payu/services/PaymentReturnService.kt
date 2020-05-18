package com.example.ps.payu.services

import com.example.ps.payu.model.PayuStatus
import java.net.URI

interface PaymentReturnService {
    fun handleUserReturn(
        paymentId: String,
        attemptId: String,
        payuStatus: PayuStatus
    ): URI
}