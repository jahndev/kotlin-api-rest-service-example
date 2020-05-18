package com.example.ps.payu.model

data class ExecutionInfo(
    val bankCode: String,
    val name: String,
    val identificationType: String,
    val identificationNumber: String,
    val phoneNumber: String,
    val userType: String
)