package com.example.ps.payu.services.helpers

import com.example.example.payment.client.dto.Payment
import com.example.example.payment.client.dto.Customer
import com.example.example.payment.client.dto.RedirectUrls
import com.example.example.payment.client.dto.Experience
import com.example.example.payment.client.dto.Order
import com.example.ps.payu.clients.paymentsos.PAYU_TOKENIZATION_TYPE
import com.example.ps.payu.clients.paymentsos.dto.AdditionalDetails
import com.example.ps.payu.clients.paymentsos.dto.PaymentsosPaymentRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuChargeRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuPaymentMethod
import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.common.PAYMENT
import com.example.ps.payu.common.ATTEMPT_ID
import com.example.ps.payu.common.MONETARY_AMOUNT_300K_FINAL
import com.example.ps.payu.common.BILLING_ADDRESS
import com.example.ps.payu.common.MONETARY_AMOUNT
import com.example.ps.payu.common.PAYU_ORDER
import com.example.ps.payu.common.SHIPPING_ADDRESS
import com.example.ps.payu.common.MERCHANT_SITE_URL
import com.example.ps.payu.common.BANK_NAME_CO
import com.example.ps.payu.common.PAYMENT_ID
import com.example.ps.payu.common.USER_FIRST_NAME
import com.example.ps.payu.common.USER_LAST_NAME
import com.example.ps.payu.common.USER_EMAIL
import com.example.ps.payu.common.USER_PHONE
import com.example.ps.payu.common.ITEM_NAME
import com.example.ps.payu.common.ORDER_SOFT_DESCRIPTION
import com.example.ps.payu.common.BANK_CODE_CO
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.common.integratorEnum
import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.Integrator.example_CO
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.services.PSP_PAYU
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PayuRequestFactoryTest {
    private val urlProvider: UrlProvider = mockk()

    private val payuBankService: PayuBankService = mockk()

    private val payuRequestFactory = PaymentsosRequestFactory(urlProvider, payuBankService)

    val banks: List<PayuBank> = listOf(PayuBank(BANK_NAME_CO, BANK_CODE_CO))

    @BeforeEach
    fun setup() {
        every { payuBankService.getBankList(any()) } returns banks
    }

    @Test
    fun `createPayuPaymentRequest works`() {
        val actual = payuRequestFactory.createPaymentRequest(payment = PAYMENT)
        val expected = PaymentsosPaymentRequest(
            MONETARY_AMOUNT_300K_FINAL,
            MONETARY_AMOUNT.currency,
            PAYMENT.order.description,
            PAYU_ORDER,
            SHIPPING_ADDRESS,
            BILLING_ADDRESS
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `given a valid Integrator get a valid bank name`() {
        val bankNameResult = payuRequestFactory.getBankName(example_CO, BANK_CODE_CO)
        assertThat(BANK_NAME_CO == bankNameResult)
    }

    @Test
    fun `createPayuChargeRequest works`() {
        every { urlProvider.createRedirectUrl(any(), any(), any(), any()) } returns MERCHANT_SITE_URL

        val payment = Payment(
            id = PAYMENT_ID,
            integrator = example_CO.name,
            amount = MONETARY_AMOUNT,
            customer = Customer(
                userId = "10",
                firstName = USER_FIRST_NAME,
                lastName = USER_LAST_NAME,
                emailAddress = USER_EMAIL,
                phoneNumber = USER_PHONE
            ),
            redirectUrls = RedirectUrls("/experience/completed", "/experience/cancelled"),
            experience = Experience("WEB", example_CO.country.languages.first().language),
            order = Order(reference = ITEM_NAME, description = ORDER_SOFT_DESCRIPTION),
            flavors = emptyList()
        )

        val executionInfo = ExecutionInfo(
            bankCode = BANK_CODE_CO,
            name = "$USER_FIRST_NAME $USER_LAST_NAME",
            identificationType = "CC",
            identificationNumber = "123456789",
            phoneNumber = USER_PHONE,
            userType = "N"
        )

        val chargeRequest = payuRequestFactory.createChargeRequest(
            payment = payment,
            attemptId = ATTEMPT_ID,
            methodType = MethodType.BANK_TRANSFER,
            executionInfo = executionInfo
        )

        val chargeRequestExpected = PayuChargeRequest(
            merchantSiteUrl = MERCHANT_SITE_URL,
            paymentMethod = PayuPaymentMethod(
                sourceType = chargeRequest.paymentMethod.sourceType,
                type = PAYU_TOKENIZATION_TYPE,
                vendor = PSP_PAYU,
                additionalDetails = AdditionalDetails(
                    bankTransferFinancialInstitutionCode = executionInfo.bankCode,
                    bankTransferFinancialInstitutionName = BANK_NAME_CO,
                    bankTransferPaymentMethodVendor = BANK_TRANSFER_VENDOR_PSE,
                    nationalIdentifyType = executionInfo.identificationType,
                    nationalIdentifyNumber = executionInfo.identificationNumber,
                    customerNationalIdentifyNumber = executionInfo.identificationNumber,
                    merchantPayerId = payment.customer.userId,
                    payerEmail = payment.customer.emailAddress,
                    orderLanguage = payment.integratorEnum().country.languages.first().language,
                    paymentCountry = payment.integratorEnum().country.countryCodeAlpha3,
                    userType = executionInfo.userType,
                    examplePaymentId = payment.id
                )
            ),
            reconciliationId = ATTEMPT_ID
        )

        assertThat(chargeRequest).isEqualTo(chargeRequestExpected)
    }
}