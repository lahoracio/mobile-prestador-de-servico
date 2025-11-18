package com.exemple.facilita.service

import com.exemple.facilita.model.AceitarServicoResponse
import com.exemple.facilita.model.Servico
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ServicoService {
    @GET("v1/facilita/servico/disponiveis")
    fun getServicosDisponiveis(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<ApiResponse>

    @PATCH("v1/facilita/servico/{id}/aceitar")
    suspend fun aceitarServicoDetalhado(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int,
        @Body body: Map<String, String> = emptyMap()
    ): Response<AceitarServicoResponse>

    @PATCH("v1/facilita/servico/{id}/aceitar")
    fun aceitarServico(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int
    ): Call<AceitarServicoApiResponse>

    @GET("v1/facilita/servico/prestador/em-andamento")
    fun getServicosEmAndamento(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<ServicosResponse>
}

// Model genérico do retorno da API para lista de serviços
data class ApiResponse(
    val status_code: Int,
    val data: List<Servico>
)

// Model para resposta de aceitar serviço (retorna objeto único)
data class AceitarServicoApiResponse(
    val status_code: Int,
    val message: String,
    val data: Servico
)

// Model para resposta de serviços em andamento
data class ServicosResponse(
    val status_code: Int,
    val servicos: List<com.exemple.facilita.model.ServicoDetalhe>
)

