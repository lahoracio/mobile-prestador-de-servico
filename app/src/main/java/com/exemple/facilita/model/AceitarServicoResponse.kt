package com.exemple.facilita.model

data class AceitarServicoResponse(
    val status_code: Int,
    val message: String,
    val data: ServicoDetalhe
)

data class ServicoDetalhe(
    val id: Int,
    val id_contratante: Int,
    val id_prestador: Int?,
    val id_categoria: Int,
    val descricao: String,
    val status: String,
    val data_solicitacao: String,
    val data_conclusao: String?,
    val data_confirmacao: String?,
    val id_localizacao: Int?,
    val valor: String,
    val tempo_estimado: Int?,
    val data_inicio: String?,
    val contratante: ContratanteDetalhe,
    val prestador: PrestadorDetalhe?,
    val categoria: CategoriaDetalhe,
    val localizacao: LocalizacaoDetalhe?
)

data class ContratanteDetalhe(
    val id: Int,
    val necessidade: String,
    val id_usuario: Int,
    val id_localizacao: Int?,
    val cpf: String,
    val usuario: UsuarioDetalhe
)

data class PrestadorDetalhe(
    val id: Int,
    val id_usuario: Int,
    val usuario: UsuarioDetalhe
)

data class UsuarioDetalhe(
    val id: Int,
    val nome: String,
    val senha_hash: String?,
    val foto_perfil: String?,
    val email: String,
    val telefone: String,
    val tipo_conta: String,
    val criado_em: String
)

data class CategoriaDetalhe(
    val id: Int,
    val nome: String,
    val descricao: String,
    val icone: String,
    val preco_base: String,
    val tempo_medio: Int
)

data class LocalizacaoDetalhe(
    val id: Int,
    val endereco: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val cep: String,
    val numero: String?,
    val complemento: String?,
    val latitude: Double,
    val longitude: Double
)

