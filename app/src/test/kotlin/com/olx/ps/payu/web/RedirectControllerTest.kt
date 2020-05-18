package com.example.ps.payu.web

import com.ninjasquad.springmockk.MockkBean
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.TRUE_CLIENT_IP_HEADER
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.model.PayuStatus
import com.example.ps.payu.services.PaymentReturnService
import com.example.ps.payu.services.helpers.UrlProvider
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI

@ExtendWith(SpringExtension::class)
@RunWith(SpringRunner::class)
@WebMvcTest(value = [RedirectController::class], excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class RedirectControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var paymentReturnService: PaymentReturnService

    @Suppress("UnusedPrivateMember")
    @MockkBean(relaxed = true)
    private lateinit var logContext: LogContext

    @Suppress("UnusedPrivateMember")
    @MockkBean
    private lateinit var urlProvider: UrlProvider

    @Test
    fun `redirect responds with HTTP 302 to the expected url`() {
        val redirectUrl = "/redirect/to/some/place"

        every {
            paymentReturnService.handleUserReturn(
                paymentId = PAYMENT_ID,
                attemptId = ATTEMPT_ID,
                payuStatus = PayuStatus.SUCCEED
            )
        } returns URI(redirectUrl)

        this.mockMvc.perform(
                get("/${PAYMENT.integrator}/bank_transfer/payu/redirect/$PAYMENT_ID/$ATTEMPT_ID" +
                        "?status={status}",
                    PayuStatus.SUCCEED)
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "superToken")
                .header(TRUE_CLIENT_IP_HEADER, "127.0.0.1")
        ).andExpect(status().`is`(302)).andExpect(header().string("Location", redirectUrl))
    }

    @Test
    fun `redirect responds with HTTP 400 if the payu-status query param is missing`() {
        this.mockMvc.perform(
            get("/${PAYMENT.integrator}/bank_transfer/payu/redirect/$PAYMENT_ID/$ATTEMPT_ID")
                .contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "superToken")
                .header(TRUE_CLIENT_IP_HEADER, "127.0.0.1")
        ).andExpect(status().`is`(400))
    }
}
