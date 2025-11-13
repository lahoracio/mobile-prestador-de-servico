package com.exemple.facilita.model

// Request para criar prestador
data class CriarPrestadorRequest(
    val localizacao: List<Int> // Lista de IDs de localização [id_endereco_mora, id_regiao_atua]
)

// Response da criação do prestador
data class CriarPrestadorResponse(
    val message: String,
    val token: String,
    val prestador: PrestadorDetalhes? = null,
    val usuario: Usuario? = null
)

data class PrestadorDetalhes(
    val id: Int,
    val id_usuario: Int,
    val usuario: Usuario? = null,
    val localizacao: List<LocalizacaoPrestador>? = null,
    val documento: List<Documento>? = null
)

data class LocalizacaoPrestador(
    val id: Int? = null,
    val logradouro: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val cep: String? = null,
    val latitude: String? = null,
    val longitude: String? = null
)

data class Documento(
    val id: Int? = null,
    val tipo_documento: String? = null,
    val valor: String? = null,
    val data_validade: String? = null,
    val arquivo_url: String? = null,
    val id_prestador: Int? = null
)


