package com.exemple.facilita.model

data class DocumentoResponse(
    val message: String,
    val documento: DocumentoData
)

data class DocumentoData(
    val id: Int,
    val tipo_documento: String,
    val valor: String,
    val data_validade: String,
    val arquivo_url: String,
    val id_prestador: Int
)

