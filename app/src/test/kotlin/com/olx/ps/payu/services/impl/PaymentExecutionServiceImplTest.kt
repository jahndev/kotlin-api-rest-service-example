package com.example.ps.payu.services.impl

import com.example.example.payment.client.AttemptClient
import com.example.example.payment.client.PaymentClient
import com.example.example.payment.client.dto.Attempt
import com.example.example.payment.client.dto.Status
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionStatus.WAITING_CUSTOMER_ACTION
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.clients.paymentsos.PaymentsosClient
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuOrder
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.MONETARY_AMOUNT
import com.example.ps.payu.common.CURRENCY_CO
import com.example.ps.payu.common.BILLING_ADDRESS
import com.example.ps.payu.common.SHIPPING_ADDRESS
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.EXECUTION_INFO
import com.example.ps.payu.common.payuChargeRequest
import com.example.ps.payu.common.MERCHANT_SITE_URL
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.HydraService
import com.example.ps.payu.services.PSP_PAYU
import com.example.ps.payu.services.PaymentExecutionService
import com.example.ps.payu.services.exceptions.PayuCommunicationException
import com.example.ps.payu.services.helpers.PaymentsosRequestFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class PaymentExecutionServiceImplTest {

    private val paymentClient = mockk<PaymentClient>()
    private val attemptClient = mockk<AttemptClient>()
    private val paymentsosClient = mockk<PaymentsosClient>()
    private val paymentsosRequestFactory = mockk<PaymentsosRequestFactory>()
    private val hydraService = mockk<HydraService>()

    val payuPaymentRequest = PaymentsosPaymentRequest(
        amount = MONETARY_AMOUNT.grossValue.longValueExact(),
        currency = CURRENCY_CO,
        order = PayuOrder("", emptyList()),
        statementSoftDescriptor = PAYMENT.order.description,
        billingAddress = BILLING_ADDRESS,
        shippingAddress = SHIPPING_ADDRESS
    )

    @BeforeEach
    fun setUp() {
        every { paymentClient.get(PAYMENT.id) } returns PAYMENT
        every { hydraService.sendEvents(any()) } returns Unit
    }

    private val paymentExecutionService: PaymentExecutionService =
        PaymentExecutionServiceImpl(
            paymentClient = paymentClient,
            attemptClient = attemptClient,
            paymentsosClient = paymentsosClient,
            paymentsosRequestFactory = paymentsosRequestFactory,
            hydraService = hydraService
        )

    @Test
    fun `executePayment with successful execution and generation of a valid ActionResult`() {
        every {
            attemptClient.create(
                Attempt(
                    paymentId = PAYMENT_ID,
                    methodType = MethodType.BANK_TRANSFER.code,
                    status = Status.WAITING_CUSTOMER_ACTION,
                    psp = PSP_PAYU
                )
            )
        } returns ATTEMPT_ID

        every {
            attemptClient.update(any(), any())
        } returns Unit

        every {
            paymentsosRequestFactory.createPaymentRequest(PAYMENT)
        } returns payuPaymentRequest

        every {
            paymentsosClient.createPayment(request = payuPaymentRequest, integrator = Integrator.example_CO)
        } returns PAYMENT_ID

        every {
            paymentsosRequestFactory.createChargeRequest(
                payment = PAYMENT,
                attemptId = ATTEMPT_ID,
                methodType = MethodType.BANK_TRANSFER,
                executionInfo = EXECUTION_INFO
            )
        } returns payuChargeRequest

        every {
            paymentsosClient.createCharge(
                payuPaymentId = PAYMENT_ID,
                payuChargeRequest = payuChargeRequest,
                integrator = Integrator.example_CO
            )
        } returns "http://www.example.com.co"

        val executePayment = paymentExecutionService.executePayment(
            paymentId = PAYMENT_ID,
            attemptId = ATTEMPT_ID,
            methodType = MethodType.BANK_TRANSFER,
            executionInfo = EXECUTION_INFO
        )

        val expected = ActionResult(
            status = WAITING_CUSTOMER_ACTION,
            actions = mapOf(
                UserOperation.SUBMIT to
                    Action(
                        type = ActionType.REDIRECT,
                        httpMethod = HttpMethod.GET,
                        contentType = null,
                        uri = MERCHANT_SITE_URL
                    )
            ),
            context = null
        )

        Assertions.assertThat(executePayment).isEqualTo(expected)
    }

    @Test
    fun `get a PayuCommunicationException when a executePayment fails`() {

        every { attemptClient.update(any(), any()) } returns Unit
        every { paymentsosRequestFactory.createPaymentRequest(PAYMENT) } returns payuPaymentRequest

        val message = "Can not create payment for request: " +
                "'$payuPaymentRequest', the response was null"

        every {
            paymentsosClient.createPayment(payuPaymentRequest, Integrator.example_CO)
        }.throws(PayuCommunicationException(PAYMENT, message, Throwable()))

        val payuCommunicationException = assertThrows<PayuCommunicationException> {
            paymentExecutionService.executePayment(
                paymentId = PAYMENT_ID,
                attemptId = ATTEMPT_ID,
                methodType = MethodType.BANK_TRANSFER,
                executionInfo = EXECUTION_INFO
            )
        }

        val expected = PayuCommunicationException(PAYMENT, "Error creating payment in payu: $message", Throwable())

        Assertions.assertThat(payuCommunicationException).isEqualToComparingFieldByField(expected)
    }
}
