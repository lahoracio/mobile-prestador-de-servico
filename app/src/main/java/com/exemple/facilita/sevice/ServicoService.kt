package com.exemple.facilita.service

import com.exemple.facilita.model.AceitarServicoResponse
import com.exemple.facilita.model.Servico
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ServicoService {
    @GET("v1/facilita/servico/disponiveis")
    fun getServicosDisponiveis(
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<ApiResponse>

    @PATCH("v1/facilita/servico/{id}/aceitar")
    suspend fun aceitarServicoDetalhado(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int,
        @Body body: Map<String, String> = emptyMap()
    ): Response<AceitarServicoResponse>

    @PATCH("v1/facilita/servico/{id}/aceitar")
    fun aceitarServico(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int
    ): Call<AceitarServicoApiResponse>

    @GET("v1/facilita/servico/meus-servicos")
    fun getServicosEmAndamento(
        @Header("Authorization") token: String
    ): Call<ServicosResponse>

    @GET("v1/facilita/servico/{id}")
    suspend fun getServicoPorId(
        @Header("Authorization") token: String,
        @Path("id") idServico: Int
    ): Response<ServicoDetalheResponse>

    @GET("v1/facilita/servico/prestador/pedidos")
    fun getHistoricoPedidos(
        @Header("Authorization") token: String,
        @Query("pagina") pagina: Int = 1,
        @Query("por_pagina") porPagina: Int = 10
    ): Call<HistoricoPedidosResponse>

    @PATCH("v1/facilita/servico/{id}/finalizar")
    suspend fun finalizarServico(
        @Path("id") idServico: Int,
        @Header("Authorization") token: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<FinalizarServicoResponse>
}

// Model genérico do retorno da API para lista de serviços
data class ApiResponse(
    val status_code: Int,
    val data: List<Servico>
)

// Model para resposta de aceitar serviço (retorna objeto único)
data class AceitarServicoApiResponse(
    val status_code: Int,
    val message: String,
    val data: Servico
)

// Model para resposta de serviços em andamento
data class ServicosResponse(
    val status_code: Int,
    val data: List<com.exemple.facilita.model.ServicoDetalhe>
)

// Model para resposta de serviço único por ID
data class ServicoDetalheResponse(
    val status_code: Int,
    val data: com.exemple.facilita.model.ServicoDetalhe
)

// Model para resposta de finalizar serviço
data class FinalizarServicoResponse(
    val status_code: Int,
    val message: String,
    val data: Any? = null
)

// Model para resposta de histórico de pedidos
data class HistoricoPedidosResponse(
    val status_code: Int,
    val data: HistoricoPedidosData
)

data class HistoricoPedidosData(
    val pedidos: List<PedidoHistorico>,
    val paginacao: Paginacao
)

data class PedidoHistorico(
    val id: Int,
    val descricao: String,
    val status: String,
    val valor: Double,
    val data_solicitacao: String,
    val data_conclusao: String?,
    val categoria: CategoriaSimples,
    val localizacao: LocalizacaoSimples?,
    val contratante: ContratanteSimples,
    val paradas: List<Parada>
)

data class CategoriaSimples(
    val id: Int,
    val nome: String
)

data class LocalizacaoSimples(
    val id: Int,
    val cidade: String
)

data class ContratanteSimples(
    val id: Int,
    val usuario: UsuarioSimples
)

data class UsuarioSimples(
    val nome: String,
    val email: String
)

data class Parada(
    val id: Int,
    val ordem: Int,
    val tipo: String,
    val lat: Double,
    val lng: Double,
    val descricao: String,
    val endereco_completo: String,
    val tempo_estimado_chegada: String?
)

data class Paginacao(
    val pagina_atual: Int,
    val total_paginas: Int,
    val total_pedidos: Int,
    val por_pagina: Int
)

