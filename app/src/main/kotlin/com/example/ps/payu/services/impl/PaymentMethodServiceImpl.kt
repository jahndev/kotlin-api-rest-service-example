package com.example.ps.payu.services.impl

import com.example.example.payment.client.dto.Payment
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.PaymentMethod
import com.example.ps.payu.services.PaymentMethodService
import com.example.ps.payu.services.helpers.PaymentMethodValidator
import com.example.ps.payu.services.helpers.UrlProvider
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class PaymentMethodServiceImpl(
    private val urlProvider: UrlProvider,
    private val paymentMethodValidator: PaymentMethodValidator
) :
    PaymentMethodService {

    override fun getAvailablePaymentMethods(payment: Payment): List<PaymentMethod> =
        MethodType.values()
            .filter { paymentMethodValidator.isPaymentSupported(it, payment) }
            .map {
                PaymentMethod(
                    methodType = it,
                    actions = mapOf(
                        UserOperation.SELECT to Action(
                            type = ActionType.REDIRECT,
                            httpMethod = HttpMethod.GET,
                            uri = urlProvider.createInitializationPageUrl(payment, it)
                        )
                    )
                )
            }

    companion object : KLogging()
}
