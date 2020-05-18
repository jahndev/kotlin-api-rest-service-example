package com.example.ps.payu.model

import java.util.Locale

enum class Integrator(val country: Country, val path: String) {
    example_CO(country = Colombia, path = "/co")
}

sealed class Country(
    val countryCode: String,
    val countryCodeAlpha3: String,
    val languages: List<Locale>
)

object Colombia : Country(
    countryCode = "CO",
    countryCodeAlpha3 = "COL",
    languages = listOf(Locale("es", "CO"))
)
