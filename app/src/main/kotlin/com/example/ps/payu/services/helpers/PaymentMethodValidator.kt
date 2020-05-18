package com.example.ps.payu.services.helpers

import com.example.example.payment.client.dto.Payment
import com.example.ps.payu.config.PseProvider
import com.example.ps.payu.model.MethodType
import org.springframework.stereotype.Component

@Component
class PaymentMethodValidator(private val pseProvider: PseProvider) {

    fun isPaymentSupported(methodType: MethodType, payment: Payment) =
        when (methodType) {
            MethodType.BANK_TRANSFER -> payment.amount.grossValue.toLong() >= pseProvider.transactionMinValue
        }
}
