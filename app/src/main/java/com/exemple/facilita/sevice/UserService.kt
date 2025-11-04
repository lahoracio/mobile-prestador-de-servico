package com.exemple.facilita.service

import com.exemple.facilita.model.ApiResponse
import com.exemple.facilita.model.CNHRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CNHService {
    @POST("v1/facilita/prestador/register")
    suspend fun registrarCNH(
        @Header("Authorization") token: String,
        @Body body: CNHRequest
    ): ApiResponse
}


