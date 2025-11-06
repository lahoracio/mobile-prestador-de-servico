package com.exemple.facilita.model

data class ModalidadeResponse(
    val message: String,
    val modalidades: List<ModalidadeData>
)

data class ModalidadeData(
    val id: Int,
    val id_prestador: Int,
    val tipo: String
)

