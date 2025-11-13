package com.exemple.facilita.service

import com.exemple.facilita.model.FinalizarCadastroResponse
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.PATCH

interface PrestadorService {
    @PATCH("v1/facilita/prestador/finalizar")
    suspend fun finalizarCadastro(
        @Header("Authorization") token: String
    ): Response<FinalizarCadastroResponse>
}

