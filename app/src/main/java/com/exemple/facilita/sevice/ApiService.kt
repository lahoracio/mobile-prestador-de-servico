package com.exemple.facilita.sevice

import com.exemple.facilita.viewmodel.PerfilViewModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("v1/facilita/usuario/perfil")
    suspend fun getProfile(@Header("Authorization") token: String): Response<PerfilViewModel.ProfileResponse>
}
