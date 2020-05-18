package com.example.ps.payu.clients.interceptors

import com.example.ps.payu.common.HEADER_NAME_CORRELATION_ID
import com.example.ps.payu.common.LogContext
import com.example.ps.payu.common.MERCHANT_SITE_URL
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution

class OutboundRequestInterceptorTest {
    private val request = mockk<HttpRequest>()
    private val httpHeaders = mockk<HttpHeaders>(relaxed = true)
    private val execution = mockk<ClientHttpRequestExecution>()
    private val logContext = mockk<LogContext>()
    private val interceptor = OutboundRequestInterceptor(logContext)

    @BeforeEach
    fun setUp() {
        every { request.headers } returns httpHeaders
        every { execution.execute(any(), any()) } returns mockk()
        every { request.uri } returns MERCHANT_SITE_URL
        every { request.method } returns HttpMethod.GET
    }

    @Test
    fun `the correlation header is set to the MDC correlation id`() {
        val correlationId = "a correlationId"

        every { logContext.correlationId } returns correlationId

        interceptor.intercept(request, ByteArray(1), execution)

        verify {
            httpHeaders.add(HEADER_NAME_CORRELATION_ID, correlationId)
        }
    }

    @Test
    fun `no correlation header is set if the MDC contains nocorrelation id`() {
        every { logContext.correlationId } returns null
        interceptor.intercept(request, ByteArray(1), execution)

        verify(exactly = 0) {
            httpHeaders.add(any(), any())
        }
    }
}