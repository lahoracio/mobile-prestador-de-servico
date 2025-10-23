package com.exemple.facilita.model

data class LoginResponse(
    val message: String,
    val token: String,
    val usuario: Usuario,
    val proximo_passo: String?
)