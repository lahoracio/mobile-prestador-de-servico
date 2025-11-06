package com.exemple.facilita.service

import com.exemple.facilita.model.DocumentoRequest
import com.exemple.facilita.model.DocumentoResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DocumentoService {
    @POST("v1/facilita/prestador/documentos")
    suspend fun cadastrarDocumento(
        @Header("Authorization") token: String,
        @Body body: DocumentoRequest
    ): DocumentoResponse
}

