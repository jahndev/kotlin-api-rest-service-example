package com.example.ps.payu.web.filters

import com.example.example.authentication.client.AuthenticationClient
import com.example.example.authentication.client.dto.Claims
import com.example.example.authentication.client.dto.ValidationRequest
import com.example.example.authentication.client.dto.ValidationResultType
import com.example.ps.payu.config.ProtectedPath
import com.example.ps.payu.config.TOKEN_HEADER
import com.example.ps.payu.config.TOKEN_PREFIX
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    authenticationManager: AuthenticationManager,
    private val authenticationClient: AuthenticationClient
) : BasicAuthenticationFilter(authenticationManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = request.getHeader(TOKEN_HEADER)?.replace(TOKEN_PREFIX, "")
        if (token != null) {
            ProtectedPath
                .values()
                .mapNotNull { it.extractPaymentId(request.requestURI) }
                .firstOrNull()
                ?.let {
                    val validationResponse = authenticationClient.validateToken(
                        ValidationRequest(
                            token = token,
                            claims = Claims(it)
                        )
                    )

                    if (validationResponse.type == ValidationResultType.VALID) {
                        val authenticationToken = UsernamePasswordAuthenticationToken(
                            "paymentExecutor_$it",
                            "payment: $it",
                            listOf(SimpleGrantedAuthority("PAYMENT_EXECUTOR"))
                        )
                        SecurityContextHolder
                            .getContext()
                            .authentication = authenticationToken
                    } else {
                        log.info("jwt token is ${validationResponse.type} for paymentId = $it")
                    }
                }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = LoggerFactory.getLogger(javaClass.enclosingClass)
    }
}
