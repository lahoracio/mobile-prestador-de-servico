package com.exemple.facilita.model

data class CNHResponse(
    val message: String,
    val cnh: CNHData? = null  // Nullable para aceitar false ou objeto
)

data class CNHData(
    val id: Int? = null,
    val id_prestador: Int? = null,
    val numero_cnh: String? = null,
    val categoria: String? = null,
    val validade: String? = null,
    val possui_ear: Boolean? = null,
    val pontuacao_atual: Int? = null,
    val data_criacao: String? = null,
    val data_atualizacao: String? = null
)

