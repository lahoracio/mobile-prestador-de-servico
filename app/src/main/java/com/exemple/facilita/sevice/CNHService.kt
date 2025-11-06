package com.exemple.facilita.service

import com.exemple.facilita.model.CNHRequest
import com.exemple.facilita.model.CNHResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CNHService {
    @POST("v1/facilita/prestador/cnh")
    suspend fun cadastrarCNH(
        @Header("Authorization") token: String,
        @Body body: CNHRequest
    ): CNHResponse
}
