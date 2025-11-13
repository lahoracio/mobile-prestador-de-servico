package com.exemple.facilita.sevice

import com.exemple.facilita.model.*
//import com.exemple.facilita.screens.ApiResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/usuario/register")
    fun saveUser(@Body user: Register): Call<RegisterResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/usuario/login")
    fun loginUser(@Body user: Login): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/usuario/recuperar-senha")
    fun recuperarSenha(@Body request: RecuperarSenhaRequest): Call<RecuperarSenhaResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/usuario/recuperar-senha")
    fun recuperarSenhaTelefone(@Body request: RecuperarSenhaTelefoneRequest): Call<RecuperarSenhaResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/usuario/verificar-codigo")
    fun verificarCodigo(@Body request: VerificarCodigoRequest): Call<VerificarSenhaResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/prestador")
    suspend fun criarPrestador(
        @Header("Authorization") token: String,
        @Body request: CriarPrestadorRequest
    ): Response<CriarPrestadorResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/facilita/localizacao")
    suspend fun criarLocalizacao(
        @Body request: LocalizacaoRequest
    ): Response<LocalizacaoResponse>

}
