package com.exemple.facilita.pagbank.api

import com.exemple.facilita.pagbank.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface de serviços da API do PagBank
 *
 * Documentação oficial: https://dev.pagseguro.uol.com.br/reference/
 */
interface PagBankService {

    // ========== PEDIDOS ==========

    /**
     * Cria um novo pedido com cobrança
     * POST /orders
     */
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Body order: PagBankOrder
    ): Response<PagBankOrder>

    /**
     * Consulta um pedido específico
     * GET /orders/{orderId}
     */
    @GET("orders/{orderId}")
    suspend fun getOrder(
        @Header("Authorization") authorization: String,
        @Path("orderId") orderId: String
    ): Response<PagBankOrder>

    // ========== QR CODE PIX ==========

    /**
     * Gera QR Code PIX para pagamento
     * POST /charges
     */
    @POST("charges")
    suspend fun createPixCharge(
        @Header("Authorization") authorization: String,
        @Body charge: PagBankCharge
    ): Response<PagBankCharge>

    /**
     * Consulta status de uma cobrança PIX
     * GET /charges/{chargeId}
     */
    @GET("charges/{chargeId}")
    suspend fun getCharge(
        @Header("Authorization") authorization: String,
        @Path("chargeId") chargeId: String
    ): Response<PagBankCharge>

    /**
     * Cancela uma cobrança
     * POST /charges/{chargeId}/cancel
     */
    @POST("charges/{chargeId}/cancel")
    suspend fun cancelCharge(
        @Header("Authorization") authorization: String,
        @Path("chargeId") chargeId: String
    ): Response<PagBankCharge>

    // ========== TRANSFERÊNCIAS ==========

    /**
     * Realiza transferência bancária
     * POST /transfers
     */
    @POST("transfers")
    suspend fun createTransfer(
        @Header("Authorization") authorization: String,
        @Body transfer: PagBankTransfer
    ): Response<PagBankTransfer>

    /**
     * Consulta status de uma transferência
     * GET /transfers/{transferId}
     */
    @GET("transfers/{transferId}")
    suspend fun getTransfer(
        @Header("Authorization") authorization: String,
        @Path("transferId") transferId: String
    ): Response<PagBankTransfer>

    /**
     * Lista transferências com filtros
     * GET /transfers
     */
    @GET("transfers")
    suspend fun listTransfers(
        @Header("Authorization") authorization: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<List<PagBankTransfer>>

    // ========== SALDO ==========

    /**
     * Consulta saldo da conta
     * GET /accounts/balance
     */
    @GET("accounts/balance")
    suspend fun getBalance(
        @Header("Authorization") authorization: String
    ): Response<PagBankBalance>

    // ========== WEBHOOKS ==========

    /**
     * Cadastra webhook para notificações
     * POST /webhooks
     */
    @POST("webhooks")
    suspend fun createWebhook(
        @Header("Authorization") authorization: String,
        @Body webhook: PagBankWebhook
    ): Response<PagBankWebhook>

    /**
     * Lista webhooks cadastrados
     * GET /webhooks
     */
    @GET("webhooks")
    suspend fun listWebhooks(
        @Header("Authorization") authorization: String
    ): Response<List<PagBankWebhook>>

    /**
     * Remove webhook
     * DELETE /webhooks/{webhookId}
     */
    @DELETE("webhooks/{webhookId}")
    suspend fun deleteWebhook(
        @Header("Authorization") authorization: String,
        @Path("webhookId") webhookId: String
    ): Response<Unit>
}

