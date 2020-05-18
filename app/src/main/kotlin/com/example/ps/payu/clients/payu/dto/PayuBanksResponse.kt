package com.example.ps.payu.clients.payu.dto

class PayuBanksResponse(
    val code: String,
    val error: String?,
    val banks: MutableList<PayuBank>? = mutableListOf()
)

class PayuBank(
    val description: String,
    val pseCode: String
)