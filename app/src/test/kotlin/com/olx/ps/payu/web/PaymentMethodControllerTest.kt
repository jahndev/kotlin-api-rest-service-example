package com.example.ps.payu.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.ContentType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.PaymentPageContext
import com.example.example.payment.hateoas.api.v2_1.UserOperation
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.EXECUTE_RESPONSE
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.PAYMENT_METHOD
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.PaymentExecutionService
import com.example.ps.payu.services.PaymentInitializationService
import com.example.ps.payu.services.PaymentMethodService
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

@ExtendWith(SpringExtension::class)
@RunWith(SpringRunner::class)
@WebMvcTest(value = [PaymentMethodController::class], excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@OverrideAutoConfiguration(enabled = false)
@ActiveProfiles("test")
class PaymentMethodControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var methodService: PaymentMethodService

    @MockkBean
    private lateinit var paymentInitializationService: PaymentInitializationService

    @MockkBean
    private lateinit var paymentExecutionService: PaymentExecutionService

    @Suppress("UnusedPrivateMember")
    @MockkBean(relaxed = true) private lateinit var urlProvider: UrlProvider

    @Suppress("UnusedPrivateMember")
    @MockkBean(relaxed = true)
    private lateinit var logContext: LogContext

    private val initializeResponse = ActionResult(
        actions = mapOf(
            UserOperation.SUBMIT to
                    Action(
                        type = ActionType.API,
                        httpMethod = HttpMethod.POST,
                        contentType = ContentType.JSON,
                        uri = URI("http://example.org/init")
                    )
        ),
        context = PaymentPageContext(
            description = PAYMENT.order.description,
            grossValue = PAYMENT.amount.grossValue,
            currency = PAYMENT.amount.currency.currencyCode,
            cancelUrl = PAYMENT.redirectUrls.experienceCancelled,
            flavors = PAYMENT.flavors
        )
    )

    @Test
    fun `getAvailablePaymentMethods responds with HTTP 200 and a Json list of methods`() {
        every {
            methodService.getAvailablePaymentMethods(PAYMENT)
        } returns listOf(PAYMENT_METHOD)
        this.mockMvc.perform(
            post("/${PAYMENT.integrator}/payu/methods")
                .contentType(APPLICATION_JSON_UTF8)
                .content(
                    objectMapper.writeValueAsString(PAYMENT)
                )
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(listOf(PAYMENT_METHOD))))
    }

    @Test
    fun `initialize responds with HTTP 200 and a init response as Json object`() {
        every {
            paymentInitializationService.initialize(PAYMENT.id, MethodType.BANK_TRANSFER)
        } returns initializeResponse

        this.mockMvc.perform(
            get("/${PAYMENT.integrator}/bank_transfer/payu/initialize/${PAYMENT.id}")
                .contentType(APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(initializeResponse)))
    }

    @Test
    fun `execute responds with HTTP 200 and a exec response as Json object`() {
        val executionInfo = ExecutionInfo(
            bankCode = "1022",
            name = "John Doe",
            identificationType = "CC",
            identificationNumber = "998877665544",
            phoneNumber = "23645963",
            userType = "N"
        )

        every {
            paymentExecutionService.executePayment(
                paymentId = PAYMENT.id,
                attemptId = ATTEMPT_ID,
                methodType = MethodType.BANK_TRANSFER,
                executionInfo = executionInfo
            )
        } returns EXECUTE_RESPONSE

        this.mockMvc.perform(
            post("/${PAYMENT.integrator}/bank_transfer/payu/execute/${PAYMENT.id}/$ATTEMPT_ID")
                .content(objectMapper.writeValueAsString(executionInfo))
                .contentType(APPLICATION_JSON_UTF8)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(EXECUTE_RESPONSE)))
    }
}
