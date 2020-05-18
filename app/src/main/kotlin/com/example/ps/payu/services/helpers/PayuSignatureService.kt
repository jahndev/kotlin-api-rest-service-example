package com.example.ps.payu.services.helpers

import com.auth0.jwt.algorithms.Algorithm
import com.example.ps.payu.clients.paymentsos.PayuCredentialsProvider
import com.example.ps.payu.model.Notification
import com.example.ps.payu.model.PayuNotification
import mu.KLogging
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Service

@Service
class PayuSignatureService(
    private val payuCredentialsProvider: PayuCredentialsProvider
) {

    fun verifySignature(
        notification: Notification,
        eventType: String,
        signature: String
    ): Boolean {
        val stringToSign = stringToSign(notification.payuNotification, eventType)
        val stringSigned = Hex.encodeHexString(
            Algorithm.HMAC256(payuCredentialsProvider
                .getPaymentsosCredentials(notification.payuNotification.appId).privateKey)
                .sign(stringToSign.toByteArray())
        ).toString()
        logger.info("string to sing: '$stringToSign' string signed: '$stringSigned'")

        return signature
            .split(",")
            .filterIndexed { index, sig -> sig.removePrefix("sig${index + 1}=") == stringSigned }
            .isNotEmpty()
    }

    private fun stringToSign(notification: PayuNotification, eventType: String) =
        with(notification) {
            listOf(
                eventType,
                id,
                accountId,
                paymentId,
                created,
                appId,
                data.id,
                data.result.status,
                data.result.category,
                data.result.subCategory,
                data.providerData.responseCode,
                data.reconciliationId,
                data.amount,
                data.currency
            ).joinToString(COMMA_STRING)
        }

    companion object : KLogging()
}

const val COMMA_STRING = ","