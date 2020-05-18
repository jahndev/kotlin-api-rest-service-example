package com.example.ps.payu.web.converters

import com.example.ps.payu.clients.paymentsos.PAYU_EVENT_TYPE
import com.example.ps.payu.clients.paymentsos.PAYU_SIGNATURE
import com.example.ps.payu.clients.paymentsos.PAYU_VERSION
import com.example.ps.payu.clients.paymentsos.X_PAYMENTS_OS_ENV
import com.example.ps.payu.clients.paymentsos.X_ZOOZ_REQUEST_ID
import com.example.ps.payu.web.requests.PayuNotificationHeaders
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.HttpMessageConversionException

fun HttpHeaders.toPayuNotificationHeaders() =
    PayuNotificationHeaders(
        signature = this[PAYU_SIGNATURE]?.first()
            ?: throwHttpException("Property '$PAYU_SIGNATURE' is missing"),
        eventType = this[PAYU_EVENT_TYPE]?.first()
            ?: throwHttpException("Property '$PAYU_EVENT_TYPE' is missing"),
        version = this[PAYU_VERSION]?.first()
            ?: throwHttpException("Property '$PAYU_VERSION' is missing"),
        paymentsOsEnv = this[X_PAYMENTS_OS_ENV]?.first()
            ?: throwHttpException("Property '$X_PAYMENTS_OS_ENV' is missing"),
        zoozRequestId = this[X_ZOOZ_REQUEST_ID]?.first()
            ?: throwHttpException("Property '$X_ZOOZ_REQUEST_ID' is missing")
    )

fun throwHttpException(message: String): String = throw HttpMessageConversionException(message)
