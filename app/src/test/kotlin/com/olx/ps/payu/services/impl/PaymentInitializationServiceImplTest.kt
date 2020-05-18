package com.example.ps.payu.services.impl

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.ContentType
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionStatus
import com.example.example.payment.hateoas.api.v2_1.PaymentListPageContext
import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.MERCHANT_SITE_URL
import com.example.ps.payu.common.BANK_NAME_CO
import com.example.ps.payu.common.BANK_CODE_CO
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.ISSUER_ID
import com.example.ps.payu.services.BANK_CODE
import com.example.ps.payu.services.NAME
import com.example.ps.payu.services.LOGO_URL
import com.example.ps.payu.services.PSP
import com.example.ps.payu.services.ONLINE
import com.example.ps.payu.services.BANK_NAME
import com.example.ps.payu.services.exceptions.NotSupportedPaymentException
import com.example.ps.payu.services.helpers.PaymentMethodValidator
import com.example.ps.payu.services.helpers.PayuBankService
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentInitializationServiceImplTest {

    private val paymentClient = mockk<PaymentClient>()
    private val attemptClient = mockk<AttemptClient>()
    private val payuBankService = mockk<PayuBankService>()
    private val urlProvider = mockk<UrlProvider>()
    private val logContext = mockk<LogContext>()
    private val paymentMethodValidator = mockk<PaymentMethodValidator>()
    private val paymentInitializationService = PaymentInitializationServiceImpl(
        paymentClient = paymentClient,
        attemptClient = attemptClient,
        payuBankService = payuBankService,
        urlProvider = urlProvider,
        logContext = logContext,
        paymentMethodValidator = paymentMethodValidator
    )

    private val action = Action(
        type = ActionType.API,
        httpMethod = HttpMethod.POST,
        contentType = ContentType.JSON,
        uri = MERCHANT_SITE_URL,
        params = mapOf(
            ISSUER_ID to BANK_TRANSFER_VENDOR_PSE,
            BANK_CODE to BANK_CODE_CO
        ),
        context = mapOf(
            NAME to MethodType.BANK_TRANSFER.name,
            LOGO_URL to "",
            PSP to com.example.ps.payu.services.PSP_PAYU,
            ONLINE to "",
            BANK_NAME to BANK_NAME_CO
        )
    )

    @BeforeEach
    internal fun setUp() {
        every { paymentClient.get(PAYMENT.id) } returns PAYMENT
        every { attemptClient.create(any()) } returns ATTEMPT_ID
        every { logContext.updateAttemptId(any()) } returns logContext
        every { urlProvider.createExecutionUrl(any(), any(), any()) } returns MERCHANT_SITE_URL
        every { payuBankService.getBankList(any()) } returns mutableListOf(PayuBank(
            description = BANK_NAME_CO,
            pseCode = BANK_CODE_CO
        ))
    }

    @Test
    fun `throws NotSupportedPaymentException when call initialize with a invalid payment amount`() {
        every { paymentMethodValidator.isPaymentSupported(any(), any()) } returns false

        val notSupportedPaymentException = assertThrows<NotSupportedPaymentException> {
            paymentInitializationService.initialize(PAYMENT.id, MethodType.BANK_TRANSFER)
        }

        val exceptionExpected = NotSupportedPaymentException(PAYMENT,
            "payment is not supported for methodType: ${MethodType.BANK_TRANSFER}")

        assertThat(notSupportedPaymentException).isEqualToComparingFieldByField(exceptionExpected)
    }

    @Test
    fun `returns a valid ActionResult when call initialize and payment is not supported by payment method`() {
        every { paymentMethodValidator.isPaymentSupported(any(), any()) } returns true

        val result = paymentInitializationService.initialize(PAYMENT.id, MethodType.BANK_TRANSFER)

        val actionResult = ActionResult(
            status = ActionStatus.CREATED,
            options = listOf<Action>(action),
            context = PaymentListPageContext(
                description = PAYMENT.order.description,
                grossValue = PAYMENT.amount.grossValue,
                currency = PAYMENT.amount.currency.toString(),
                cancelUrl = PAYMENT.redirectUrls.experienceCancelled
            )
        )

        assertThat(result).isEqualTo(actionResult)
    }
}
