package com.exemple.facilita.model

data class DadosContratante(
    val id: Int,
    val cpf: String,
    val necessidade: String,
    val localizacao: Localizacao
)

