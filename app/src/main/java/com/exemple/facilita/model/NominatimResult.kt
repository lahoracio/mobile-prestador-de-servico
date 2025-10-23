package com.exemple.facilita.model

data class NominatimResult(
    val display_name: String,
    val lat: String,
    val lon: String,
    val address: Address? = null
)

data class Address(
    val road: String? = null,
    val house_number: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null
)

