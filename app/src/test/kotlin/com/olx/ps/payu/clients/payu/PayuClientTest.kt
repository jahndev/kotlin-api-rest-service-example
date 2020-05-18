package com.example.ps.payu.clients.payu

import com.ninjasquad.springmockk.MockkBean
import com.example.ps.payu.clients.paymentsos.PayuCredentialsProvider
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.payu.dto.PayuBankListInformation
import com.example.ps.payu.clients.payu.dto.PayuBanksRequest
import com.example.ps.payu.clients.payu.dto.PayuBanksResponse
import com.example.ps.payu.clients.payu.dto.PayuMerchantCredentials
import com.example.ps.payu.common.payuAccount
import com.example.ps.payu.common.bankList
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.model.Integrator
import io.mockk.every
import io.mockk.slot
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
class PayuClientTest {

    @Autowired
    private lateinit var payuClient: PayuClient

    @MockkBean
    @Qualifier(value = "payuRestTemplate")
    private lateinit var restTemplate: RestTemplate

    @MockkBean(relaxed = true)
    private lateinit var payuCredentialsProvider: PayuCredentialsProvider

    val capturedUrl = slot<String>()
    val capturedHttpEntity = slot<HttpEntity<PaymentsosPaymentRequest>>()

    @BeforeEach
    fun setUp() {
        every {
            payuCredentialsProvider.getPayuCredentials(any())
        } returns payuAccount
    }

    @Test
    fun `given correct request getBanks returns a valid bank list`() {
        val payuBanksResponse = PayuBanksResponse(
            code = "SUCCESS",
            error = null,
            banks = bankList
        )
        every {
            restTemplate.postForObject(
                capture(capturedUrl),
                capture(capturedHttpEntity),
                any<Class<PayuBanksResponse>>()
            )
        } returns payuBanksResponse

        val payuBanksRequest = PayuBanksRequest(
            test = true,
            language = Integrator.example_CO.country.languages.first().language,
            command = "GET_BANKS_LIST",
            merchant = PayuMerchantCredentials(
                apiLogin = payuAccount.apiLogin,
                apiKey = payuAccount.apiKey
            ),
            bankListInformation = PayuBankListInformation(
                paymentMethod = BANK_TRANSFER_VENDOR_PSE,
                paymentCountry = Integrator.example_CO.country.countryCode
            )
        )

        val response = payuClient.getBanks(payuBanksRequest)

        Assertions.assertThat(response == bankList)
    }
}