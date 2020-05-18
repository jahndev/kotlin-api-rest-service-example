package com.example.ps.payu.clients.paymentsos

import com.example.ps.payu.model.Integrator
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.naming.ConfigurationException

@Suppress("LateinitUsage")
@ConfigurationProperties(prefix = "credentials")
@Component
class PayuCredentialsProvider {

    var paymentsos: MutableList<PaymentsosCountryCredential> = mutableListOf()
    var payu: MutableList<PayuAccount> = mutableListOf()

    fun getPaymentsosCredentials(appId: String) = paymentsos.first { it.appId == appId }

    fun getPayuCredentials(integrator: Integrator) = payu.first { it.integrator == integrator.name }

    fun getPaymentsosCredentials(integrator: Integrator): PaymentsosCountryCredential = paymentsos.find {
        it.integrator.endsWith(integrator.name)
    } ?: throw ConfigurationException("Cannot found PaymentsOS credential for $integrator")
}

@Suppress("LateinitUsage")
class PaymentsosCountryCredential {
    lateinit var integrator: String
    lateinit var appId: String
    lateinit var privateKey: String
    lateinit var environment: String
}

@Suppress("LateinitUsage")
class PayuAccount {
    lateinit var integrator: String
    lateinit var apiLogin: String
    lateinit var apiKey: String
}