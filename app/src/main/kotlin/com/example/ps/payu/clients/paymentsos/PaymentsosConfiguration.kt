package com.example.ps.payu.clients.paymentsos

import org.springframework.stereotype.Component

@Component
class PaymentsosConfiguration(
    val baseUrl: String = "https://api.paymentsos.com",
    val apiVersion: String = "1.2.0",
    val paymentsOsEnv: String = "test",
    val clientIp: String = "127.0.0.1"
)

const val PRIVATE_KEY = "private_key"
const val IDEMPOTENCY_KEY = "idempotency_key"
const val API_VERSION = "api-version"
const val X_CLIENT_IP_ADDRESS = "x-client-ip-address"
const val X_PAYMENTS_OS_ENV = "x-payments-os-env"
const val APP_ID = "app_id"
const val X_ZOOZ_REQUEST_ID = "x-zooz-request-id"
const val PAYU_SIGNATURE = "signature"
const val PAYU_EVENT_TYPE = "event-type"
const val PAYU_VERSION = "version"
const val PAYU_TOKENIZATION_TYPE = "untokenized"
