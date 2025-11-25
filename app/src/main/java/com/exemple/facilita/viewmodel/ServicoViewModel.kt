package com.exemple.facilita.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.ServicoDetalhe
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicoState(
    val isLoading: Boolean = false,
    val servico: ServicoDetalhe? = null,
    val error: String? = null
)

class ServicoViewModel : ViewModel() {
    private val TAG = "ServicoViewModel"

    private val _servicoState = MutableStateFlow(ServicoState())
    val servicoState: StateFlow<ServicoState> = _servicoState.asStateFlow()

    private val _servicosAceitos = MutableStateFlow<Map<Int, ServicoDetalhe>>(emptyMap())
    val servicosAceitos: StateFlow<Map<Int, ServicoDetalhe>> = _servicosAceitos.asStateFlow()

    fun carregarServico(servicoId: Int, context: Context? = null) {
        viewModelScope.launch {
            _servicoState.value = _servicoState.value.copy(isLoading = true)
            Log.d(TAG, "")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔍 CARREGANDO SERVIÇO")
            Log.d(TAG, "   ServicoId: $servicoId")
            Log.d(TAG, "   Context fornecido: ${context != null}")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            try {
                // Primeiro, tentar buscar o serviço aceito no cache
                val servicoCache = _servicosAceitos.value[servicoId]
                Log.d(TAG, "📦 Cache contém ${_servicosAceitos.value.size} serviços")
                Log.d(TAG, "📦 IDs no cache: ${_servicosAceitos.value.keys}")

                if (servicoCache != null) {
                    Log.d(TAG, "✅ Serviço encontrado no cache")
                    _servicoState.value = ServicoState(
                        isLoading = false,
                        servico = servicoCache,
                        error = null
                    )
                } else if (context != null) {
                    // Se não estiver no cache e context foi fornecido, buscar da API
                    Log.d(TAG, "📡 Serviço não está no cache, buscando da API...")
                    val token = TokenManager.obterTokenComBearer(context)
                    Log.d(TAG, "🔑 Token disponível: ${token != null}")

                    if (token != null) {
                        val service = RetrofitFactory.getServicoService()
                        Log.d(TAG, "🌐 Chamando API: GET /v1/facilita/servico/$servicoId")
                        val response = service.getServicoPorId(token, servicoId)

                        Log.d(TAG, "📡 Resposta da API:")
                        Log.d(TAG, "   Status Code: ${response.code()}")
                        Log.d(TAG, "   Is Successful: ${response.isSuccessful}")
                        Log.d(TAG, "   Body is null: ${response.body() == null}")

                        if (response.isSuccessful && response.body() != null) {
                            val servico = response.body()!!.data
                            Log.d(TAG, "✅ Serviço carregado da API com sucesso")
                            Log.d(TAG, "   ID: ${servico.id}")
                            Log.d(TAG, "   Descrição: ${servico.descricao}")
                            Log.d(TAG, "   Status: ${servico.status}")

                            // Salvar no cache para próximas consultas
                            salvarServicoAceito(servico)

                            _servicoState.value = ServicoState(
                                isLoading = false,
                                servico = servico,
                                error = null
                            )
                            Log.d(TAG, "✅ Estado atualizado com sucesso")
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = "Erro ao carregar serviço: ${response.code()}"
                            Log.e(TAG, "❌ $errorMsg")
                            Log.e(TAG, "❌ Error body: $errorBody")
                            _servicoState.value = ServicoState(
                                isLoading = false,
                                servico = null,
                                error = errorMsg
                            )
                        }
                    } else {
                        Log.e(TAG, "❌ Token não encontrado")
                        _servicoState.value = ServicoState(
                            isLoading = false,
                            servico = null,
                            error = "Token não encontrado. Faça login novamente."
                        )
                    }
                } else {
                    Log.e(TAG, "❌ Serviço não encontrado no cache e context não fornecido")
                    _servicoState.value = ServicoState(
                        isLoading = false,
                        servico = null,
                        error = "Serviço não encontrado"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exceção ao carregar serviço: ${e.message}", e)
                _servicoState.value = ServicoState(
                    isLoading = false,
                    servico = null,
                    error = e.message ?: "Erro ao carregar serviço"
                )
            }
        }
    }

    fun salvarServicoAceito(servicoDetalhe: ServicoDetalhe) {
        viewModelScope.launch {
            val novosServicos = _servicosAceitos.value.toMutableMap()
            novosServicos[servicoDetalhe.id] = servicoDetalhe
            _servicosAceitos.value = novosServicos
        }
    }

    fun limparEstado() {
        _servicoState.value = ServicoState()
    }

    // Estado para finalização de serviço
    private val _finalizarServicoState = MutableStateFlow<FinalizarServicoState>(FinalizarServicoState.Idle)
    val finalizarServicoState: StateFlow<FinalizarServicoState> = _finalizarServicoState.asStateFlow()

    fun finalizarServico(servicoId: Int, context: Context, valorServico: Double? = null) {
        viewModelScope.launch {
            _finalizarServicoState.value = FinalizarServicoState.Loading
            Log.d(TAG, "")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🏁 FINALIZANDO SERVIÇO")
            Log.d(TAG, "   ServicoId: $servicoId")
            Log.d(TAG, "   Valor: R$ $valorServico")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            try {
                val token = TokenManager.obterTokenComBearer(context)
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Token não encontrado")
                    _finalizarServicoState.value = FinalizarServicoState.Error("Token não encontrado. Faça login novamente.")
                    return@launch
                }

                val usuarioId = TokenManager.obterUsuarioId(context)
                if (usuarioId == null) {
                    Log.e(TAG, "❌ ID do usuário não encontrado")
                    _finalizarServicoState.value = FinalizarServicoState.Error("ID do usuário não encontrado.")
                    return@launch
                }

                Log.d(TAG, "🔑 Token obtido: ${token.take(20)}...")
                Log.d(TAG, "👤 Usuario ID: $usuarioId")
                Log.d(TAG, "📡 Chamando API para finalizar serviço...")

                val response = RetrofitFactory.getServicoService().finalizarServico(servicoId, token)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Serviço finalizado com sucesso")
                    Log.d(TAG, "   Response code: ${response.code()}")

                    // Adicionar valor à carteira do prestador se valor foi informado
                    if (valorServico != null && valorServico > 0) {
                        Log.d(TAG, "💰 Adicionando R$ $valorServico à carteira do prestador...")

                        try {
                            val solicitacaoDeposito = com.exemple.facilita.model.SolicitacaoDeposito(
                                valor = valorServico,
                                metodoPagamento = "SERVICO",
                                comprovante = "Serviço #$servicoId finalizado"
                            )

                            val carteiraResponse = RetrofitFactory.getCarteiraService()
                                .solicitarDeposito(solicitacaoDeposito, token)

                            if (carteiraResponse.isSuccessful) {
                                Log.d(TAG, "✅ Valor R$ $valorServico adicionado à carteira com sucesso!")
                                val transacao = carteiraResponse.body()
                                if (transacao != null) {
                                    Log.d(TAG, "   Transação ID: ${transacao.id}")
                                    Log.d(TAG, "   Status: ${transacao.status}")
                                    Log.d(TAG, "   Tipo: ${transacao.tipo}")
                                }
                            } else {
                                val errorBody = carteiraResponse.errorBody()?.string()
                                Log.e(TAG, "⚠️ Erro ao adicionar valor à carteira: ${carteiraResponse.code()}")
                                Log.e(TAG, "   Error body: $errorBody")
                                // Não falha a finalização do serviço por isso
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "⚠️ Exceção ao adicionar valor à carteira: ${e.message}", e)
                            // Não falha a finalização do serviço por isso
                        }
                    } else {
                        Log.d(TAG, "ℹ️ Valor do serviço não informado, pulando adição à carteira")
                    }

                    // Remover do cache de serviços aceitos
                    val novosServicos = _servicosAceitos.value.toMutableMap()
                    novosServicos.remove(servicoId)
                    _servicosAceitos.value = novosServicos

                    _finalizarServicoState.value = FinalizarServicoState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Erro ao finalizar serviço: ${response.code()}")
                    Log.e(TAG, "   Error body: $errorBody")
                    _finalizarServicoState.value = FinalizarServicoState.Error("Erro ao finalizar serviço: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exceção ao finalizar serviço: ${e.message}", e)
                _finalizarServicoState.value = FinalizarServicoState.Error(e.message ?: "Erro ao finalizar serviço")
            }
        }
    }

    fun resetFinalizarState() {
        _finalizarServicoState.value = FinalizarServicoState.Idle
    }
}

sealed class FinalizarServicoState {
    object Idle : FinalizarServicoState()
    object Loading : FinalizarServicoState()
    object Success : FinalizarServicoState()
    data class Error(val message: String) : FinalizarServicoState()
}

