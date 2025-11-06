package com.exemple.facilita.model

data class DocumentoRequest(
    val tipo_documento: String,
    val valor: String,
    val data_validade: String,
    val arquivo_url: String
)

