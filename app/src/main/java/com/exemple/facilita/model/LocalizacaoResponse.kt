package com.exemple.facilita.model


data class LocalizacaoResponse(
    val id: Int,
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: String,
    val longitude: String
)