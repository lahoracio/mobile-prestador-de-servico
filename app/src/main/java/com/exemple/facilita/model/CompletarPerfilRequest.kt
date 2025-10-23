package com.exemple.facilita.model

data class CompletarPerfilRequest(
    val id_localizacao: Int,
    val necessidade: String,
    val cpf: String
)