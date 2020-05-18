package com.example.ps.payu.clients.paymentsos

import com.example.ps.payu.common.APP_ID_CO
import com.example.ps.payu.common.paymentsosCountryCredential
import com.example.ps.payu.model.Integrator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class PayuCredentialsProviderTest {

    @Autowired
    private val payuCredentialsProvider = PayuCredentialsProvider()

    @Test
    fun `given a valid integrator then getPaymentsosCredentials return valid credentials`() {
        val credentials = payuCredentialsProvider.getPaymentsosCredentials(Integrator.example_CO)

        assertThat(credentials == paymentsosCountryCredential)
    }

    @Test
    fun `given a valid appId then getPayuCredentials return valid credentials`() {
        val credentials = payuCredentialsProvider.getPaymentsosCredentials(APP_ID_CO)

        assertThat(credentials == paymentsosCountryCredential)
    }

    @Test
    fun `given a valid Integrator then getPayuCredentials return valid credentials`() {
        val expected = PayuAccount().apply {
            integrator = Integrator.example_CO.name
            apiLogin = "login"
            apiKey = "key"
        }
        val credentials = payuCredentialsProvider.getPayuCredentials(Integrator.example_CO)

        assertThat(credentials == expected)
    }
}