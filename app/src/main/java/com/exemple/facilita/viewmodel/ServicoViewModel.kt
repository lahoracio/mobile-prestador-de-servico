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

                        // Tentar buscar em "meus serviços" primeiro (serviços aceitos pelo prestador)
                        try {
                            Log.d(TAG, "🌐 Chamando API: GET /v1/facilita/servico/meus-servicos")

                            // Usar suspend function
                            val meusServicosResponse = service.getMeusServicos(token)

                            if (meusServicosResponse.isSuccessful && meusServicosResponse.body() != null) {
                                val meusServicos = meusServicosResponse.body()!!.data
                                val servicoEncontrado = meusServicos.find { it.id == servicoId }

                                if (servicoEncontrado != null) {
                                    Log.d(TAG, "✅ Serviço encontrado em 'meus serviços'")
                                    Log.d(TAG, "   ID: ${servicoEncontrado.id}")
                                    Log.d(TAG, "   Descrição: ${servicoEncontrado.descricao}")
                                    Log.d(TAG, "   Status: ${servicoEncontrado.status}")

                                    // Salvar no cache
                                    salvarServicoAceito(servicoEncontrado)

                                    _servicoState.value = ServicoState(
                                        isLoading = false,
                                        servico = servicoEncontrado,
                                        error = null
                                    )
                                    return@launch
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ Erro ao buscar em 'meus serviços', tentando serviços disponíveis: ${e.message}")
                        }

                        // Se não encontrou em "meus serviços", tentar nos serviços disponíveis
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
                        Log.d(TAG, "💰 Processando pagamento de R$ $valorServico...")

                        try {
                            // Criar instância do CarteiraViewModel para processar o pagamento
                            val carteiraViewModel = com.exemple.facilita.viewmodel.CarteiraViewModel(
                                context.applicationContext as android.app.Application
                            )

                            // Adicionar pagamento diretamente à carteira local
                            carteiraViewModel.adicionarPagamentoServico(
                                usuarioId = usuarioId.toString(),
                                valorServico = valorServico,
                                servicoId = servicoId
                            )

                            Log.d(TAG, "✅ Pagamento processado!")
                            Log.d(TAG, "   Saldo atualizado localmente")
                            Log.d(TAG, "   Transação registrada")

                        } catch (e: Exception) {
                            Log.e(TAG, "⚠️ Erro ao processar pagamento localmente: ${e.message}", e)
                            // Fallback: tentar via API
                            try {
                                Log.d(TAG, "🔄 Tentando fallback via API...")
                                val solicitacaoDeposito = com.exemple.facilita.model.SolicitacaoDeposito(
                                    valor = valorServico,
                                    metodoPagamento = "SERVICO",
                                    comprovante = "Serviço #$servicoId finalizado"
                                )

                                val carteiraResponse = RetrofitFactory.getCarteiraService()
                                    .solicitarDeposito(solicitacaoDeposito, token)

                                if (carteiraResponse.isSuccessful) {
                                    Log.d(TAG, "✅ Fallback bem-sucedido via API")
                                } else {
                                    Log.e(TAG, "❌ Fallback API falhou: ${carteiraResponse.code()}")
                                }
                            } catch (fallbackError: Exception) {
                                Log.e(TAG, "❌ Fallback API exception: ${fallbackError.message}")
                            }
                        }
                    } else {
                        Log.d(TAG, "ℹ️ Valor do serviço não informado, pulando pagamento")
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

    // Sobrecarga com callbacks diretos para facilitar uso em Composables
    fun finalizarServico(
        servicoId: Int,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(TAG, "")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🏁 FINALIZANDO SERVIÇO")
            Log.d(TAG, "   ServicoId: $servicoId")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            try {
                val token = TokenManager.obterTokenComBearer(context)
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Token não encontrado")
                    onError("Token não encontrado. Faça login novamente.")
                    return@launch
                }

                Log.d(TAG, "🔑 Token obtido: ${token.take(20)}...")
                Log.d(TAG, "📡 Chamando API PATCH /servico/$servicoId/finalizar")

                val response = RetrofitFactory.getServicoService().finalizarServico(servicoId, token)

                Log.d(TAG, "📡 Resposta recebida:")
                Log.d(TAG, "   Status Code: ${response.code()}")
                Log.d(TAG, "   Is Successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "✅ Serviço finalizado com sucesso!")
                    Log.d(TAG, "   Status Code: ${responseBody?.status_code}")
                    Log.d(TAG, "   Mensagem: ${responseBody?.message}")

                    // Remover do cache de serviços aceitos
                    val novosServicos = _servicosAceitos.value.toMutableMap()
                    novosServicos.remove(servicoId)
                    _servicosAceitos.value = novosServicos
                    Log.d(TAG, "📦 Serviço removido do cache")

                    // Chamar callback de sucesso
                    onSuccess()
                    Log.d(TAG, "✅ Callback onSuccess executado")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "Erro ${response.code()}: ${response.message()}"
                    Log.e(TAG, "❌ Erro ao finalizar serviço")
                    Log.e(TAG, "   Código: ${response.code()}")
                    Log.e(TAG, "   Mensagem: ${response.message()}")
                    Log.e(TAG, "   Body: $errorBody")

                    onError(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exceção ao finalizar serviço: ${e.message}", e)
                onError(e.message ?: "Erro ao finalizar serviço")
            }
        }
    }
}

sealed class FinalizarServicoState {
    object Idle : FinalizarServicoState()
    object Loading : FinalizarServicoState()
    object Success : FinalizarServicoState()
    data class Error(val message: String) : FinalizarServicoState()
}
