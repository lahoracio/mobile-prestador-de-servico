package com.exemple.facilita.model

data class ModalidadeRequest(
    val modalidades: List<ModalidadeItem>
)

data class ModalidadeItem(
    val tipo: String,
    val modelo_veiculo: String? = null,
    val ano_veiculo: Int? = null,
    val possui_seguro: Boolean? = null,
    val compartimento_adequado: Boolean? = null,
    val revisao_em_dia: Boolean? = null,
    val antecedentes_criminais: Boolean? = null
)

