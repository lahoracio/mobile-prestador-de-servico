package com.exemple.facilita.service

import com.exemple.facilita.model.Servico
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ServicoService {
    @GET("v1/facilita/servico/disponiveis")
    fun getServicosDisponiveis(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<ApiResponse>

    @PATCH("v1/facilita/servico/{id}/aceitar")
    fun aceitarServico(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int
    ): Call<ApiResponse>
}

// Model genérico do retorno da API
data class ApiResponse(
    val status_code: Int,
    val data: List<Servico>
)
