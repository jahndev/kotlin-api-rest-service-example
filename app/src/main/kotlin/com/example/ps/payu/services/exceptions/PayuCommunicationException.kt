package com.example.ps.payu.services.exceptions

import com.example.example.payment.client.dto.Payment

class PayuCommunicationException(
    val payment: Payment,
    message: String,
    cause: Throwable
) : RuntimeException(message, cause)