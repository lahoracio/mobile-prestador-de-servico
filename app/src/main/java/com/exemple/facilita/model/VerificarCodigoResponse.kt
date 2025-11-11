package com.exemple.facilita.model

data class VerificarSenhaResponse(
    val message: String,
    val sucesso: Boolean,
    val dados: DadosVerificacao
)

data class DadosVerificacao(
    val usuario_id: Int,
    val email: String,
    val telefone: String,
    val codigo_valido: String
)
