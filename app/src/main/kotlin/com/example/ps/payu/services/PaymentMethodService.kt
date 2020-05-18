package com.example.ps.payu.services

import com.example.example.payment.client.dto.Payment
import com.example.ps.payu.model.PaymentMethod

interface PaymentMethodService {
    fun getAvailablePaymentMethods(payment: Payment): List<PaymentMethod>
}
