package com.exemple.facilita.model

data class Servico(
    val id: Int,
    val id_contratante: Int,
    val id_prestador: Int?,
    val id_categoria: Int,
    val descricao: String,
    val status: String,
    val data_solicitacao: String,
    val valor: String,
    val contratante: Contratante,
    val categoria: Categoria,
    val localizacao: Localizacao?
)

data class Contratante(
    val id: Int,
    val necessidade: String,
    val usuario: Usuario
)

data class Usuario(
    val id: Int,
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val telefone: String
)

data class Categoria(
    val id: Int,
    val nome: String,
    val descricao: String,
    val icone: String,
    val preco_base: String,
    val tempo_medio: Int
)

data class Localizacao(
    val id: Int,
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: String,
    val longitude: String
)
