package com.example.ps.payu.config

import com.example.example.authentication.client.AuthenticationClient
import com.example.ps.payu.web.filters.JwtAuthenticationFilter
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
@ConditionalOnProperty("features.enforce-authentication")
class SecurityConfiguration(private val authenticationClient: AuthenticationClient) :
    WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.let {
            it.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .regexMatchers(*ProtectedPath.values().map { it.pathPattern }.toTypedArray()).authenticated()
                .anyRequest().permitAll()
                .and()
                .addFilter(JwtAuthenticationFilter(authenticationManager(), authenticationClient))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
    }
}

@Configuration
@ConditionalOnProperty("features.enforce-authentication", havingValue = "false", matchIfMissing = true)
@EnableAutoConfiguration(exclude = [SecurityAutoConfiguration::class, ManagementWebSecurityAutoConfiguration::class])
class DisableSecurityConfiguration

enum class ProtectedPath(val pathPattern: String) {

    INITIALIZE(".*/payu/initialize/(.+)"),
    EXECUTE(".*/payu/execute/(.+)/(.+)");

    private val pattern = ".*?$pathPattern".toRegex().toPattern()

    fun extractPaymentId(path: String) =
        pattern.matcher(path).let { if (it.matches()) it.group(1) else null }
}
