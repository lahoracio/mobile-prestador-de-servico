package com.exemple.facilita.model

data class UpdatePerfilRequest(
    val email: String? = null,
    val telefone: String? = null,
    val cidade: String? = null
)

data class UpdatePerfilResponse(
    val status_code: Int,
    val message: String,
    val data: PerfilContratanteData?
)

