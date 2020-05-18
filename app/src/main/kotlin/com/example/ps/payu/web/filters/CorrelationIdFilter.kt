package com.example.ps.payu.web.filters

import com.example.ps.payu.common.HEADER_NAME_CORRELATION_ID
import com.example.ps.payu.common.LogContext
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(1)
class CorrelationIdFilter(
    private val logContext: LogContext
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = request.getHeader(HEADER_NAME_CORRELATION_ID) ?: UUID.randomUUID().toString()
        logContext.updateCorrelationId(correlationId)
        filterChain.doFilter(request, response)
    }
}
