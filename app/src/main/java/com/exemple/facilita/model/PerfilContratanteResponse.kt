package com.exemple.facilita.model

data class PerfilContratanteResponse(
    val status_code: Int,
    val data: PerfilContratanteData
)

data class PerfilContratanteData(
    val id: Int,
    val nome: String,
    val email: String,
    val telefone: String,
    val foto_perfil: String?,
    val tipo_conta: String,
    val criado_em: String,
    val carteira: String?,
    val dados_contratante: DadosContratante
)

