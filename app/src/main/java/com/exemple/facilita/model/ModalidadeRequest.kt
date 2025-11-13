package com.exemple.facilita.model

data class Modalidade(
    val tipo: String, // MOTO, CARRO, BICICLETA
    val modelo_veiculo: String,
    val ano_veiculo: Int,
    val possui_seguro: Boolean,
    val compartimento_adequado: Boolean,
    val revisao_em_dia: Boolean,
    val antecedentes_criminais: Boolean
)

data class ModalidadeRequest(
    val modalidades: List<Modalidade>
)

