package com.exemple.facilita.model

import com.google.gson.annotations.SerializedName

// Modelo flexível que aceita diferentes estruturas da API
data class PerfilPrestadorResponse(
    val id: Int? = null,
    val nome: String? = null,
    val email: String? = null,
    val celular: String? = null,
    @SerializedName("tipo_conta")
    val tipoConta: String? = null,
    val status: String? = null,
    val prestador: PrestadorInfo? = null,
    // Campos adicionais que podem vir da API
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    val cpf: String? = null,
    val cnh: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class PrestadorInfo(
    val id: Int? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    @SerializedName("foto_perfil")
    val fotoPerfil: String? = null,
    val cnh: String? = null,
    @SerializedName("tipo_veiculo")
    val tipoVeiculo: String? = null,
    @SerializedName("placa_veiculo")
    val placaVeiculo: String? = null
)

data class AtualizarPerfilRequest(
    val nome: String? = null,
    val email: String? = null,
    val celular: String? = null,
    val endereco: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)

data class AtualizarPerfilResponse(
    val message: String? = null,
    val usuario: PerfilPrestadorResponse? = null
)

