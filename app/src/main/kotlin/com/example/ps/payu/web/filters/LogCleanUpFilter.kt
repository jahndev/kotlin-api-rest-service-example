package com.example.ps.payu.web.filters

import com.example.ps.payu.common.LogContext
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LogCleanUpFilter(private val logContext: LogContext) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            logContext.clear()
        }
    }
}
