package com.example.ps.payu.services.impl

import com.example.example.payment.client.dto.MonetaryAmount
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.common.CURRENCY_CO
import com.example.ps.payu.common.MERCHANT_SITE_URL
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.config.PseProvider
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.PaymentMethod
import com.example.ps.payu.services.helpers.PaymentMethodValidator
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

val MIN_TRANSACTION_VALUE_VALID_FOR_PSE_BANK_TRANSFER: BigDecimal = BigDecimal.valueOf(1600)

class PaymentMethodServiceImplTest {

    private var urlProvider = mockk<UrlProvider>()
    private var pseProvider = mockk<PseProvider>()
    private var paymentMethodValidator = PaymentMethodValidator(pseProvider)
    private val paymentMethodService = PaymentMethodServiceImpl(urlProvider, paymentMethodValidator)

    private val paymentMethod = PaymentMethod(
        methodType = MethodType.BANK_TRANSFER,
        actions = mapOf(
            UserOperation.SELECT to Action(
                type = ActionType.REDIRECT,
                httpMethod = HttpMethod.GET,
                uri = MERCHANT_SITE_URL
            )
        )
    )

    @BeforeEach
    internal fun setUp() {
        every { urlProvider.createInitializationPageUrl(any(), any()) } returns MERCHANT_SITE_URL
        every {
            pseProvider.transactionMinValue
        } returns MIN_TRANSACTION_VALUE_VALID_FOR_PSE_BANK_TRANSFER.longValueExact()
    }

    @Test
    fun `responds with the all methods for a supported payment`() {
        val expectedPaymentMethod: List<PaymentMethod> = listOf(paymentMethod)
        val methods = paymentMethodService.getAvailablePaymentMethods(PAYMENT)

        assertThat(methods).isEqualTo(expectedPaymentMethod)
    }

    @ParameterizedTest
    @ValueSource(longs = [1000, 1600, 2000])
    fun `responds with methods for payment with valid amount`(amountValue: Long) {
        val expectedPaymentMethod: List<PaymentMethod> = listOf(paymentMethod)

        val payment = PAYMENT.copy(
            amount = MonetaryAmount(grossValue = BigDecimal.valueOf(amountValue), currency = CURRENCY_CO)
        )

        val methods = paymentMethodService.getAvailablePaymentMethods(payment)

        if (payment.amount.grossValue < MIN_TRANSACTION_VALUE_VALID_FOR_PSE_BANK_TRANSFER)
            assertThat(methods).isEmpty()
        else assertThat(methods).isEqualTo(expectedPaymentMethod)
    }
}
