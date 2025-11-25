package com.exemple.facilita.model

import com.google.gson.annotations.SerializedName

// Resposta da API para o perfil do prestador
data class PerfilPrestadorResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    val data: PerfilPrestadorData
)

data class PerfilPrestadorData(
    val id: Int,
    val nome: String,
    val email: String,
    val telefone: String,
    @SerializedName("foto_perfil")
    val fotoPerfil: String?,
    @SerializedName("tipo_conta")
    val tipoConta: String,
    @SerializedName("criado_em")
    val criadoEm: String,
    val carteira: Carteira?,
    @SerializedName("dados_prestador")
    val dadosPrestador: DadosPrestador?
)

data class DadosPrestador(
    val id: Int,
    val ativo: Boolean,
    val documentos: List<Documento>,
    val cnh: List<CNH>,
    val modalidades: List<ModalidadeServico>,
    val localizacoes: List<LocalizacaoPrestador>
)

data class Documento(
    val id: Int,
    val tipo: String,
    val numero: String,
    val url: String?
)

data class CNH(
    val id: Int,
    val numero: String,
    val validade: String,
    val categoria: String,
    val url: String?
)

data class ModalidadeServico(
    val id: Int,
    val nome: String,
    val descricao: String?
)

data class LocalizacaoPrestador(
    val id: Int,
    val logradouro: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val cep: String,
    val latitude: String,
    val longitude: String
)

data class AtualizarPerfilRequest(
    val nome: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)

data class AtualizarPerfilResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    val message: String,
    val data: PerfilPrestadorData?
)

