package com.exemple.facilita.model

data class CNHRequest(
    val numero_cnh: String,
    val categoria: String,
    val validade: String,
    val possui_ear: Boolean
)

