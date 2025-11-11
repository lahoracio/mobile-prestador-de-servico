package com.exemple.facilita.model

data class Usuario(
    val id: Int,
    val nome: String,
    val email: String?,
    val celular: String?,
    val tipo_conta: String,
    val status: String?
)

