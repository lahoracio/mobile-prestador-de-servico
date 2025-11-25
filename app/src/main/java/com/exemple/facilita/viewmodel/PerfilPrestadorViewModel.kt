package com.exemple.facilita.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.AtualizarPerfilRequest
import com.exemple.facilita.model.PerfilPrestadorData
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilPrestadorViewModel : ViewModel() {

    private val TAG = "PerfilPrestadorViewModel"

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Idle)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    fun carregarPerfil(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "")
            Log.d(TAG, "╔═══════════════════════════════════════╗")
            Log.d(TAG, "║   INICIANDO CARREGAMENTO DO PERFIL   ║")
            Log.d(TAG, "╚═══════════════════════════════════════╝")

            withContext(Dispatchers.Main) {
                _uiState.value = PerfilUiState.Loading
            }
            Log.d(TAG, "Estado alterado para: Loading")

            try {
                // 1. VERIFICAR TOKEN
                Log.d(TAG, "")
                Log.d(TAG, "📋 PASSO 1: Verificando token...")
                // SharedPreferences é thread-safe, pode ser acessado de qualquer thread
                val token = TokenManager.obterTokenComBearer(context)

                if (token == null) {
                    Log.e(TAG, "❌ ERRO: Token é NULL!")
                    withContext(Dispatchers.Main) {
                        _uiState.value = PerfilUiState.Error("Token não encontrado. Faça login novamente.")
                    }
                    return@launch
                }

                Log.d(TAG, "✅ Token encontrado: ${token.take(50)}...")
                Log.d(TAG, "   Tamanho do token: ${token.length} caracteres")

                // 2. FAZER REQUISIÇÃO (já estamos em Dispatchers.IO)
                Log.d(TAG, "")
                Log.d(TAG, "🌐 PASSO 2: Fazendo requisição HTTP...")
                Log.d(TAG, "   URL Base: https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net/")
                Log.d(TAG, "   Endpoint: GET /v1/facilita/usuario/perfil")
                Log.d(TAG, "   Header: Authorization: ${token.take(30)}...")
                Log.d(TAG, "   Thread atual: ${Thread.currentThread().name}")

                val response = RetrofitFactory.userService.obterPerfil(token)

                // 3. ANALISAR RESPOSTA
                Log.d(TAG, "")
                Log.d(TAG, "📡 PASSO 3: Resposta recebida")
                Log.d(TAG, "   Código HTTP: ${response.code()}")
                Log.d(TAG, "   Mensagem: ${response.message()}")
                Log.d(TAG, "   Sucesso: ${response.isSuccessful}")
                Log.d(TAG, "   Body é null: ${response.body() == null}")


                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val perfil = apiResponse.data

                    Log.d(TAG, "")
                    Log.d(TAG, "✅ SUCESSO! Dados recebidos:")
                    Log.d(TAG, "╔════════════════════════════════════════")
                    Log.d(TAG, "║ Status Code: ${apiResponse.statusCode}")
                    Log.d(TAG, "║ ID: ${perfil.id}")
                    Log.d(TAG, "║ Nome: ${perfil.nome}")
                    Log.d(TAG, "║ Email: ${perfil.email}")
                    Log.d(TAG, "║ Telefone: ${perfil.telefone}")
                    Log.d(TAG, "║ Tipo Conta: ${perfil.tipoConta}")
                    Log.d(TAG, "║ Foto Perfil: ${perfil.fotoPerfil ?: "NULL"}")
                    Log.d(TAG, "║ Criado Em: ${perfil.criadoEm}")

                    if (perfil.dadosPrestador != null) {
                        val dados = perfil.dadosPrestador
                        Log.d(TAG, "║ Dados Prestador:")
                        Log.d(TAG, "║   - ID: ${dados.id}")
                        Log.d(TAG, "║   - Ativo: ${dados.ativo}")
                        Log.d(TAG, "║   - Documentos: ${dados.documentos.size}")
                        Log.d(TAG, "║   - CNH: ${dados.cnh.size}")
                        Log.d(TAG, "║   - Modalidades: ${dados.modalidades.size}")
                        Log.d(TAG, "║   - Localizações: ${dados.localizacoes.size}")

                        if (dados.localizacoes.isNotEmpty()) {
                            Log.d(TAG, "║   Localizações:")
                            dados.localizacoes.forEachIndexed { index, loc ->
                                Log.d(TAG, "║     [$index] ${loc.logradouro}, ${loc.numero} - ${loc.bairro}")
                                Log.d(TAG, "║         ${loc.cidade} - CEP: ${loc.cep}")
                            }
                        }
                    } else {
                        Log.d(TAG, "║ Dados Prestador: NULL")
                    }
                    Log.d(TAG, "╚════════════════════════════════════════")

                    withContext(Dispatchers.Main) {
                        _uiState.value = PerfilUiState.Success(perfil)
                    }
                    Log.d(TAG, "Estado alterado para: Success")

                } else {
                    val errorBody = response.errorBody()?.string()

                    Log.e(TAG, "")
                    Log.e(TAG, "❌ ERRO NA RESPOSTA:")
                    Log.e(TAG, "╔════════════════════════════════════════")
                    Log.e(TAG, "║ Código: ${response.code()}")
                    Log.e(TAG, "║ Mensagem: ${response.message()}")
                    Log.e(TAG, "║ Headers: ${response.headers()}")
                    Log.e(TAG, "║ Error Body: $errorBody")
                    Log.e(TAG, "╚════════════════════════════════════════")

                    val mensagemErro = when (response.code()) {
                        401 -> "Sessão expirada. Faça login novamente."
                        404 -> "Endpoint não encontrado. O backend não tem este endpoint."
                        500 -> "Erro no servidor. Verifique os logs do backend."
                        else -> "Erro HTTP ${response.code()}"
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.value = PerfilUiState.Error(mensagemErro)
                    }
                    Log.e(TAG, "Estado alterado para: Error - $mensagemErro")
                }

            } catch (e: Exception) {
                Log.e(TAG, "")
                Log.e(TAG, "❌ EXCEÇÃO CAPTURADA:")
                Log.e(TAG, "╔════════════════════════════════════════")
                Log.e(TAG, "║ Tipo: ${e.javaClass.simpleName}")
                Log.e(TAG, "║ Mensagem: ${e.message}")
                Log.e(TAG, "║ Stack Trace:")
                e.printStackTrace()
                Log.e(TAG, "╚════════════════════════════════════════")

                withContext(Dispatchers.Main) {
                    _uiState.value = PerfilUiState.Error("Erro: ${e.message}")
                }
                Log.e(TAG, "Estado alterado para: Error")
            }

            Log.d(TAG, "")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "    FIM DO CARREGAMENTO DO PERFIL")
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "")
        }
    }

    fun atualizarPerfil(
        context: Context,
        nome: String? = null,
        email: String? = null,
        telefone: String? = null,
        endereco: String? = null,
        cidade: String? = null,
        estado: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _isUpdating.value = true
                }

                // SharedPreferences é thread-safe
                val token = TokenManager.obterTokenComBearer(context)

                if (token == null) {
                    withContext(Dispatchers.Main) {
                        onError("Token não encontrado. Faça login novamente.")
                        _isUpdating.value = false
                    }
                    return@launch
                }

                val request = AtualizarPerfilRequest(
                    nome = nome,
                    email = email,
                    telefone = telefone,
                    endereco = endereco,
                    cidade = cidade,
                    estado = estado
                )

                // Já estamos em Dispatchers.IO
                val response = RetrofitFactory.userService.atualizarPerfil(token, request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // Atualiza o estado com os novos dados
                        val apiResponse = response.body()
                        val usuario = apiResponse?.data

                        if (usuario != null) {
                            _uiState.value = PerfilUiState.Success(usuario)
                            onSuccess()
                        } else {
                            Log.e(TAG, "Resposta de atualização não contém dados do usuário")
                            onError("Erro ao obter dados atualizados do perfil.")
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            401 -> "Sessão expirada. Faça login novamente."
                            400 -> "Dados inválidos. Verifique os campos."
                            else -> "Erro ao atualizar perfil: ${response.code()}"
                        }
                        onError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Erro de conexão: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isUpdating.value = false
                }
            }
        }
    }

    sealed class PerfilUiState {
        object Idle : PerfilUiState()
        object Loading : PerfilUiState()
        data class Success(val perfil: PerfilPrestadorData) : PerfilUiState()
        data class Error(val message: String) : PerfilUiState()
    }
}

