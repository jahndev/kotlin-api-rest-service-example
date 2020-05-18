package com.example.ps.payu.common

import com.example.example.payment.client.dto.Attempt
import com.example.example.payment.client.dto.Customer
import com.example.example.payment.client.dto.Experience
import com.example.example.payment.client.dto.MonetaryAmount
import com.example.example.payment.client.dto.Order
import com.example.example.payment.client.dto.Payment
import com.example.example.payment.client.dto.RedirectUrls
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.ActionResult
import com.example.example.payment.hateoas.api.v2_1.ActionType
import com.example.example.payment.hateoas.api.v2_1.ContentType
import com.example.example.payment.hateoas.api.v2_1.HttpMethod
import com.example.example.payment.hateoas.api.v2_1.PaymentPageContext
import com.example.example.payment.hateoas.api.v2_1.UserOperation

import com.example.ps.payu.clients.paymentsos.PAYU_TOKENIZATION_TYPE
import com.example.ps.payu.clients.paymentsos.PaymentsosCountryCredential
import com.example.ps.payu.clients.paymentsos.PayuAccount

import com.example.ps.payu.clients.paymentsos.dto.PayuAddress
import com.example.ps.payu.clients.paymentsos.dto.PayuPaymentMethod
import com.example.ps.payu.clients.paymentsos.dto.AdditionalDetails
import com.example.ps.payu.clients.paymentsos.dto.PayuBillingAddress
import com.example.ps.payu.clients.paymentsos.dto.Redirection
import com.example.ps.payu.clients.paymentsos.dto.PayuChargeRequest
import com.example.ps.payu.clients.paymentsos.dto.PayuOrder
import com.example.ps.payu.clients.paymentsos.dto.PayuItem
import com.example.ps.payu.clients.payu.dto.PayuBank

import com.example.ps.payu.model.ExecutionInfo
import com.example.ps.payu.model.Integrator
import com.example.ps.payu.model.MethodType
import com.example.ps.payu.model.PaymentMethod
import com.example.ps.payu.model.PayuData
import com.example.ps.payu.model.PayuNotification

import com.example.ps.payu.services.PSP_PAYU
import java.math.BigDecimal
import java.net.URI
import java.util.Currency

const val API_GATEWAY_BASE_URL = "http://127.0.0.1:8080"
const val FRONTEND_BASE_URL = "http://127.0.0.1:3000"
const val PAYMENT_ID = "1"
const val example_PAYMENT_ID = "10"
const val ATTEMPT_ID = "2"
const val BANK_NAME_CO = "BANCO UNION COLOMBIANO"
const val BANK_CODE_CO = "1022"
const val APP_ID_CO = "com.example.example_co"
const val PRIVATE_KEY_CO = "f281c8de-fa69-4a65-bcad-3a5a2111fd6a"
const val TRUE_CLIENT_IP_HEADER = "True-Client-IP"

val MERCHANT_SITE_URL = URI("http://www.example.com.co")
val EXTERNAL_PSE_URL = URI("http://www.pse.com.co")
val CURRENCY_CO: Currency = Currency.getInstance("COP")
val MONETARY_AMOUNT_300K = BigDecimal.valueOf(300000)
val MONETARY_AMOUNT_300K_FINAL = BigDecimal.valueOf(300000).movePointRight(2).toLong()

const val USER_EMAIL = "johndoe@gmail.com"
const val USER_FIRST_NAME = "jhon"
const val USER_LAST_NAME = "doe"
const val USER_PHONE = "573555000"

const val ITEM_NAME = "item name"
const val ITEM_QUANTITY = 1
const val ITEM_UNIT_PRICE = 30000000

val SHIPPING_ADDRESS = PayuAddress(
    firstName = USER_FIRST_NAME,
    lastName = USER_LAST_NAME,
    email = USER_EMAIL,
    phone = USER_PHONE
)

val BILLING_ADDRESS = PayuBillingAddress(Integrator.example_CO.country.countryCodeAlpha3, USER_EMAIL)

val MONETARY_AMOUNT = MonetaryAmount(grossValue = MONETARY_AMOUNT_300K, currency = CURRENCY_CO)

const val ORDER_SOFT_DESCRIPTION = "soft description"

val ORDER = Order(reference = ITEM_NAME, description = ORDER_SOFT_DESCRIPTION)

val PAYMENT = Payment(
    id = PAYMENT_ID,
    integrator = Integrator.example_CO.name,
    amount = MONETARY_AMOUNT,
    customer = Customer(
        userId = "10",
        firstName = USER_FIRST_NAME,
        lastName = USER_LAST_NAME,
        emailAddress = USER_EMAIL,
        phoneNumber = USER_PHONE
    ),
    redirectUrls = RedirectUrls("/experience/completed", "/experience/cancelled"),
    experience = Experience("WEB", Integrator.example_CO.country.languages.first().language),
    order = ORDER,
    flavors = emptyList()
)

val ATTEMPT = Attempt(
    paymentId = PAYMENT_ID,
    psp = PSP_PAYU,
    methodType = MethodType.BANK_TRANSFER.name,
    subtype = null
)

val EXECUTE_RESPONSE = ActionResult(
    options = listOf(
        Action(
            type = ActionType.API,
            httpMethod = HttpMethod.POST,
            contentType = ContentType.JSON,
            uri = URI("http://example.org/exec")
        )
    ),
    context = PaymentPageContext(
        description = PAYMENT.order.description,
        grossValue = PAYMENT.amount.grossValue,
        currency = PAYMENT.amount.currency.currencyCode,
        cancelUrl = PAYMENT.redirectUrls.experienceCancelled,
        flavors = PAYMENT.flavors
    )
)

val PAYMENT_METHOD = PaymentMethod(
    methodType = MethodType.BANK_TRANSFER,
    actions = mapOf(
        UserOperation.SELECT to
            Action(
                type = ActionType.REDIRECT,
                uri = URI("/${MethodType.BANK_TRANSFER}"),
                httpMethod = HttpMethod.GET
            )
    )
)

val EXECUTION_INFO = ExecutionInfo(
    bankCode = BANK_CODE_CO,
    name = "$USER_FIRST_NAME $USER_LAST_NAME",
    identificationType = "CC",
    identificationNumber = "123456789",
    phoneNumber = USER_PHONE,
    userType = "N"
)

val REDIRECTION = Redirection(EXTERNAL_PSE_URL)

val ADDITIONAL_DETAILS = AdditionalDetails(
    bankTransferFinancialInstitutionCode = EXECUTION_INFO.bankCode,
    bankTransferFinancialInstitutionName = BANK_NAME_CO,
    bankTransferPaymentMethodVendor = BANK_TRANSFER_VENDOR_PSE,
    nationalIdentifyType = EXECUTION_INFO.identificationType,
    nationalIdentifyNumber = EXECUTION_INFO.identificationNumber,
    customerNationalIdentifyNumber = EXECUTION_INFO.identificationNumber,
    merchantPayerId = PAYMENT.customer.userId,
    payerEmail = PAYMENT.customer.emailAddress.toString(),
    orderLanguage = PAYMENT.integratorEnum().country.languages.first().language,
    paymentCountry = PAYMENT.integratorEnum().country.countryCodeAlpha3,
    userType = EXECUTION_INFO.userType,
    examplePaymentId = PAYMENT.id
)

val PAYU_PAYMENT_METHOD = PayuPaymentMethod(
    sourceType = MethodType.BANK_TRANSFER.name,
    type = PAYU_TOKENIZATION_TYPE,
    vendor = PSP_PAYU,
    additionalDetails = ADDITIONAL_DETAILS
)

val payuChargeRequest = PayuChargeRequest(
    merchantSiteUrl = MERCHANT_SITE_URL,
    paymentMethod = PAYU_PAYMENT_METHOD,
    reconciliationId = ATTEMPT_ID
)

val paymentsosCountryCredential = PaymentsosCountryCredential().apply {
    integrator = Integrator.example_CO.name
    appId = APP_ID_CO
    privateKey = PRIVATE_KEY_CO
    environment = "test"
}

val payuAccount = PayuAccount().apply {
    integrator = Integrator.example_CO.name
    apiLogin = "login"
    apiKey = "key"
}

val bankList = mutableListOf(PayuBank(BANK_NAME_CO, BANK_CODE_CO))

fun getPayuData(resultStatus: String = "Succeed") = PayuData(
    id = "",
    result = PayuData.PayuResult(
        status = resultStatus,
        category = "",
        subCategory = ""
    ),
    providerSpecificData = PayuData.PayuProviderSpecificData(additionalDetails = ADDITIONAL_DETAILS),
    reconciliationId = "",
    providerData = PayuData.PayuProviderData(responseCode = ""),
    amount = "",
    currency = ""
)

fun getPayuNotification(resultStatus: String = "Succeed") =
    PayuNotification(
        id = "1",
        created = "123",
        paymentId = "20",
        accountId = "myAccountId",
        appId = "myAppId",
        data = getPayuData(resultStatus)
    )

val PAYU_ORDER = PayuOrder(
    id = PAYMENT.order.reference,
    lineItems = listOf(
        PayuItem(ITEM_NAME, ITEM_QUANTITY, ITEM_UNIT_PRICE)
    )
)