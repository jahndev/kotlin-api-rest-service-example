package com.example.ps.payu.services.helpers

import com.example.ps.payu.clients.paymentsos.PayuAccount
import com.example.ps.payu.clients.paymentsos.PayuCredentialsProvider
import com.example.ps.payu.clients.payu.PayuClient
import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.common.BANK_CODE_CO
import com.example.ps.payu.common.BANK_NAME_CO
import com.example.ps.payu.common.paymentsosCountryCredential
import com.example.ps.payu.model.Integrator
import io.mockk.mockk
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PayuBankServiceTest {

    private val payuClient = mockk<PayuClient>()
    private val payuCredentialsProvider = mockk<PayuCredentialsProvider>()
    val payuBankService = PayuBankService(payuClient, payuCredentialsProvider)

    @Test
    fun `given a valid integrator verify that getBankList return valid List of PayuBank`() {

        val payuAccount = PayuAccount().apply {
            integrator = "example_CO"
            apiLogin = "login"
            apiKey = "key"
        }

        val expected = mutableListOf(PayuBank(BANK_NAME_CO, BANK_CODE_CO))

        every {
            payuCredentialsProvider.getPaymentsosCredentials(any<Integrator>())
        } returns paymentsosCountryCredential
        every { payuCredentialsProvider.getPayuCredentials(any()) } returns payuAccount
        every { payuClient.getBanks(any()) } returns expected

        val bankList = payuBankService.getBankList(Integrator.example_CO)

        assertThat(bankList).isEqualTo(expected)
    }
}