package com.example.ps.payu.services.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.example.ps.payu.common.ADDITIONAL_DETAILS
import org.junit.jupiter.api.Test

class PiiManagerTest {
    private val objectMapper = ObjectMapper()
    private val piiManager = PiiManager(objectMapper)

    @Test
    fun `get an additional_details object and return an string with pii redacted `() {

        val result = piiManager.redactPii(ADDITIONAL_DETAILS)
        val redacted = "\":\"REDACTED"

        assert(with(result) {
                contains("national_identify_type$redacted") ||
                contains("customer_national_identify_number$redacted") ||
                contains("merchant_payer_id$redacted") ||
                contains("payer_email$redacted")
            }
        )
    }
}