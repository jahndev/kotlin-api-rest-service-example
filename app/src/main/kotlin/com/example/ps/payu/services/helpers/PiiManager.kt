package com.example.ps.payu.services.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ValueNode
import com.fasterxml.jackson.module.kotlin.convertValue
import org.springframework.stereotype.Component

@Component
class PiiManager(private val objectMapper: ObjectMapper) {

    fun redactPii(any: Any): String = any.toJsonNode().redactPiiValues().serialize()

    private fun Any.toJsonNode() = objectMapper.convertValue<JsonNode>(this)

    private fun JsonNode.serialize() =
        if (this is TextNode) this.textValue()
        else objectMapper.writeValueAsString(this)

    private fun JsonNode.redactPiiValues(): JsonNode {
        when (this.nodeType) {
            JsonNodeType.ARRAY -> processArray()
            JsonNodeType.OBJECT -> processObject()
            else -> Unit
        }
        return this
    }

    private fun JsonNode.processArray() {
        this.elements().forEach {
            it.redactPiiValues()
        }
    }

    private fun JsonNode.processObject() {
        this.fields().forEach {
            when {
                keyShouldBeRedacted(it.key) -> if (it.value != null && it.value != NullNode.instance)
                    it.setValue(TextNode("REDACTED"))
                it.value is ValueNode -> if (it.value.asText().contains("card", true))
                    it.setValue(TextNode("REDACTED"))
                else -> it.value.redactPiiValues()
            }
        }
    }

    private fun keyShouldBeRedacted(text: String?) =
        !text.isNullOrBlank() && blacklistedPiiKeys.contains(text.toLowerCase().trim())

    private val blacklistedPiiKeys = setOf(
        "expiryDate",
        "cardSummary",
        "card",
        "paymentMethod",
        "shopperIP",
        "email",
        "shopperReference",
        "user",
        "name",
        "cvc",
        "address",
        "fingerprint",
        "socialSecurityNumber",
        "dateOfBirth",
        "phone",
        "bankAccount",
        "recurringDetailReference",
        "expiryDate",
        "authCode",
        "card.encrypted.json",
        "deliveryAddress",
        "billingAddress",
        "deviceFingerprint",
        "shopperName",
        "shopperEmail",
        "telephoneNumber",
        "userId",
        "firstName",
        "lastName",
        "emailAddress",
        "phoneNumber",
        "customerId",
        "encryptedCustomerInput",
        "cardNumber",
        "merchant_payer_id",
        "national_identify_number",
        "nationalIdentifyNumber",
        "customerNationalIdentifyNumber",
        "customer_national_identify_number",
        "nationalIdentifyType",
        "national_identify_type",
        "ip_address",
        "pseReference3",
        "telephone",
        "payer_email",
        "apiLogin",
        "apiKey"
    ).map { it.toLowerCase() }
}
