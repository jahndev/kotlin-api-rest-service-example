package com.example.ps.payu.clients.payu.dto

data class PayuBanksRequest(
    val test: Boolean,
    val language: String = "es",
    val command: String = "GET_BANKS_LIST",
    val merchant: PayuMerchantCredentials,
    val bankListInformation: PayuBankListInformation
)

data class PayuMerchantCredentials(
    val apiLogin: String,
    val apiKey: String
)

data class PayuBankListInformation(
    val paymentMethod: String,
    val paymentCountry: String
)