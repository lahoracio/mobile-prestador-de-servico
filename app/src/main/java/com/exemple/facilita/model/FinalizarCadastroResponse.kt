package com.exemple.facilita.model

data class FinalizarCadastroResponse(
    val message: String,
    val prestador: PrestadorCompleto
)

data class PrestadorCompleto(
    val id: Int,
    val id_usuario: Int,
    val ativo: Boolean,
    val documento: List<DocumentoCadastrado>,
    val cnh: List<CNHCadastrada>,
    val modalidades: List<ModalidadeCadastrada>
)

data class DocumentoCadastrado(
    val id: Int,
    val tipo_documento: String,
    val valor: String,
    val data_validade: String? = null,
    val arquivo_url: String? = null,
    val id_prestador: Int
)

data class CNHCadastrada(
    val id: Int,
    val id_prestador: Int,
    val numero_cnh: String,
    val categoria: String,
    val validade: String,
    val possui_ear: Boolean,
    val pontuacao_atual: Int,
    val data_criacao: String,
    val data_atualizacao: String
)

data class ModalidadeCadastrada(
    val id: Int,
    val id_prestador: Int,
    val tipo: String
)

