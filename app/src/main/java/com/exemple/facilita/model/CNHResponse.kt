package com.exemple.facilita.model

data class CNHResponse(
    val message: String,
    val cnh: CNHData
)

data class CNHData(
    val id: Int? = null,
    val id_prestador: Int? = null,
    val numero_cnh: String,
    val categoria: String,
    val validade: String,
    val possui_ear: Boolean,
    val pontuacao_atual: Int? = null,
    val data_criacao: String? = null,
    val data_atualizacao: String? = null
)

