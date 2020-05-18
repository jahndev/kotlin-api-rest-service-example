package com.example.ps.payu.services.helpers

import com.example.ps.payu.clients.paymentsos.PayuCredentialsProvider
import com.example.ps.payu.clients.payu.PayuClient
import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.clients.payu.dto.PayuBankListInformation
import com.example.ps.payu.clients.payu.dto.PayuBanksRequest
import com.example.ps.payu.clients.payu.dto.PayuMerchantCredentials
import com.example.ps.payu.common.BANK_TRANSFER_VENDOR_PSE
import com.example.ps.payu.model.Integrator
import mu.KLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

const val COMMAND_GET_BANKS_LIST = "GET_BANKS_LIST"

@Service
class PayuBankService(
    private val payuClient: PayuClient,
    private val payuCredentialsProvider: PayuCredentialsProvider
) {
    @Cacheable(cacheNames = ["bankList"])
    fun getBankList(integrator: Integrator): List<PayuBank> {
        logger.info("loading bank list from payu")
        val paymentsosCredentials = payuCredentialsProvider.getPaymentsosCredentials(integrator)
        val payuCredentials = payuCredentialsProvider.getPayuCredentials(integrator)
        val payuBanksRequest = PayuBanksRequest(
            test = paymentsosCredentials.environment == "test",
            language = integrator.country.languages.first().language,
            command = COMMAND_GET_BANKS_LIST,
            merchant = PayuMerchantCredentials(
                apiLogin = payuCredentials.apiLogin,
                apiKey = payuCredentials.apiKey
            ),
            bankListInformation = PayuBankListInformation(
                paymentMethod = BANK_TRANSFER_VENDOR_PSE,
                paymentCountry = integrator.country.countryCode
            )
        )
        return payuClient.getBanks(payuBanksRequest)
    }

    companion object : KLogging()
}