package com.example.ps.payu.web.filters

import com.example.example.authentication.client.AuthenticationClient
import com.example.example.authentication.client.dto.ValidationResponse
import com.example.example.authentication.client.dto.ValidationResultType
import com.example.example.authentication.client.dto.ValidationResultType.VALID
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.config.TOKEN_HEADER
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilterTest {
    private val defaultToken = "someToken"
    private val defaultTokenHeader = "Bearer $defaultToken"
    private val defaultUri = "/payu/execute/$PAYMENT_ID/$ATTEMPT_ID"

    private val authenticationManager = mockk<AuthenticationManager>()
    private val authenticationClient = mockk<AuthenticationClient>()
    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>()
    private val filterChain = mockk<FilterChain>(relaxed = true)

    @BeforeEach
    fun setUp() {
        every { request.getAttribute(any()) } returns null
        every { request.getHeader(TOKEN_HEADER) } returns defaultTokenHeader
        every { request.requestURI } returns defaultUri
        every { authenticationClient.validateToken(any()) } returns ValidationResponse("", VALID)
    }

    private val authFilter = JwtAuthenticationFilter(
        authenticationManager = authenticationManager,
        authenticationClient = authenticationClient
    )

    @Test
    fun `a valid token authenticates the caller`() {
        authFilter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNotNull
        assertThat(authentication.principal).isEqualTo("paymentExecutor_$PAYMENT_ID")
        assertThat(authentication.credentials).isEqualTo("payment: $PAYMENT_ID")
        assertThat(authentication.authorities.any { it.authority == "PAYMENT_EXECUTOR" }).isEqualTo(true)

        verify {
            authenticationClient.validateToken(withArg {
                it.token == defaultToken &&
                it.claims.paymentId == PAYMENT_ID
            })
        }
    }

    @Test
    fun `a call without a token is not authenticated`() {
        every { request.getHeader(TOKEN_HEADER) } returns null

        authFilter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNull()
    }

    @ParameterizedTest
    @EnumSource(value = ValidationResultType::class, names = ["INVALID", "EXPIRED"])
    fun `a call without a valid token is not authenticated`(resultType: ValidationResultType) {
        every { authenticationClient.validateToken(any()) } returns ValidationResponse("", resultType)

        authFilter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNull()
    }
}