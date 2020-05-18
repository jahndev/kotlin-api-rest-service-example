package com.example.ps.payu.services.exceptions

import com.example.example.payment.client.dto.Payment
import java.lang.RuntimeException

class NotSupportedPaymentException(val payment: Payment, message: String) : RuntimeException(message)
