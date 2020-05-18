package com.example.ps.payu.common

import com.example.example.payment.client.dto.Payment
import com.example.ps.payu.model.Integrator

fun Payment.integratorEnum() = Integrator.valueOf(this.integrator)

fun Payment.toInitParameter() = """
    |{
    |   "paymentId": "${this.id}",
    |   "integrator": "${this.integrator.toLowerCase()}",
    |   "platform": "${this.experience.platform}",
    |   "locale": "${this.experience.locale}"
    |}""".trimMargin().base64Encode()
