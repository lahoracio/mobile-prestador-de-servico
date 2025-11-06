package com.exemple.facilita.service

import com.exemple.facilita.model.ModalidadeRequest
import com.exemple.facilita.model.ModalidadeResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ModalidadeService {
    @POST("v1/facilita/prestador/modalidades")
    suspend fun cadastrarModalidades(
        @Header("Authorization") token: String,
        @Body body: ModalidadeRequest
    ): ModalidadeResponse
}

