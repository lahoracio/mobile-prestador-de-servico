package com.exemple.facilita.model

data class Register(
    val nome: String,
    val email: String,
    val telefone: String,
    val senha_hash: String,
    val foto_perfil: String
)
