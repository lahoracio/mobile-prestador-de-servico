package com.exemple.facilita.model

data class CNHRequest(
    val localizacao: List<Int>,
    val modalidades: List<String>,
    val cnh: CNHData,
    val documento: List<DocumentoData>
)

data class CNHData(
    val numero_cnh: String,
    val categoria: String,
    val validade: String,
    val possui_ear: Boolean
)

data class DocumentoData(
    val tipo_documento: String,
    val valor: String,
    val data_validade: String? = null,
    val arquivo_url: String? = null
)

data class ApiResponse(
    val message: String? = null,
    val success: Boolean? = null
)
