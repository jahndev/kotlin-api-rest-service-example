package com.example.ps.payu.web.filters

import com.example.ps.payu.common.HEADER_NAME_CORRELATION_ID
import com.example.ps.payu.common.LogContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CorrelationIdFilterTest {
    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>()
    private val filterChain = mockk<FilterChain>(relaxed = true)
    private val logContext = spyk<LogContext>()

    @BeforeEach
    fun setUp() {
        every { request.getAttribute(any()) } returns null
    }

    private val correlationIdFilter = CorrelationIdFilter(logContext)

    @Test
    fun `a provided correlationId is added to the logging context`() {
        val correlationId = UUID.randomUUID().toString()
        every { request.getHeader(HEADER_NAME_CORRELATION_ID) } returns correlationId
        correlationIdFilter.doFilter(request, response, filterChain)

        verify {
            logContext.updateCorrelationId(correlationId)
        }
    }

    @Test
    fun `a call without a correlationId creates a new one`() {
        every { request.getHeader(HEADER_NAME_CORRELATION_ID) } returns null

        correlationIdFilter.doFilter(request, response, filterChain)

        verify {
            logContext.updateCorrelationId(any())
        }
    }
}