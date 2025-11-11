package com.exemple.facilita.model

data class RegisterResponse(
    val message: String,
    val token: String,
    val usuario: Usuario,
    val proximo_passo: String?,
    val seguranca: Seguranca
)



