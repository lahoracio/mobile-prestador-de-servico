package com.exemple.facilita.api

import com.exemple.facilita.model.*
import retrofit2.Response
import retrofit2.http.*

interface CarteiraService {

    @GET("api/carteira/{usuarioId}")
    suspend fun getCarteira(@Path("usuarioId") usuarioId: String): Response<Carteira>

    @GET("api/carteira/{usuarioId}/transacoes")
    suspend fun getTransacoes(
        @Path("usuarioId") usuarioId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<Transacao>>

    @POST("api/carteira/saque")
    suspend fun solicitarSaque(
        @Body solicitacao: SolicitacaoSaque,
        @Header("Authorization") token: String
    ): Response<Transacao>

    @POST("api/carteira/deposito")
    suspend fun solicitarDeposito(
        @Body solicitacao: SolicitacaoDeposito,
        @Header("Authorization") token: String
    ): Response<Transacao>

    @GET("api/conta-bancaria/{usuarioId}")
    suspend fun getContasBancarias(@Path("usuarioId") usuarioId: String): Response<List<ContaBancaria>>

    @POST("api/conta-bancaria")
    suspend fun adicionarContaBancaria(
        @Body conta: ContaBancaria,
        @Header("Authorization") token: String
    ): Response<ContaBancaria>

    @PUT("api/conta-bancaria/{contaId}")
    suspend fun atualizarContaBancaria(
        @Path("contaId") contaId: String,
        @Body conta: ContaBancaria,
        @Header("Authorization") token: String
    ): Response<ContaBancaria>

    @DELETE("api/conta-bancaria/{contaId}")
    suspend fun removerContaBancaria(
        @Path("contaId") contaId: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("api/conta-bancaria/{contaId}/principal")
    suspend fun definirContaPrincipal(
        @Path("contaId") contaId: String,
        @Header("Authorization") token: String
    ): Response<Unit>
}

