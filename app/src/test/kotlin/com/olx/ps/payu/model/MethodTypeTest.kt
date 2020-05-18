package com.example.ps.payu.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MethodTypeTest {

    @Test
    fun `throw IllegalArgumentException when method type code is invalid`() {
        val invalidMethodTypeCode = "unknown"

        val illegalArgumentException = assertThrows<IllegalArgumentException> {
            MethodType.fromCode(invalidMethodTypeCode)
        }

        assertThat(illegalArgumentException.message).isEqualTo("unknown code $invalidMethodTypeCode")
    }
}
