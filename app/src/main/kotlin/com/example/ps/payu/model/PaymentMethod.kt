package com.example.ps.payu.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.example.example.payment.hateoas.api.v2_1.API_VERSION
import com.example.example.payment.hateoas.api.v2_1.Action
import com.example.example.payment.hateoas.api.v2_1.UserOperation

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentMethod(
    val version: String = API_VERSION,
    val methodType: MethodType,
    val context: Map<String, String>? = null,
    val actions: Map<UserOperation, Action>,
    val psp: String = "payu"
)

enum class MethodType(val code: String) {
    BANK_TRANSFER(code = "bank_transfer");

    companion object {
        fun fromCode(code: String) =
            values().find { it.code == code }
                ?: throw IllegalArgumentException("unknown code $code")
    }
}
