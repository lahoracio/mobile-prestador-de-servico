package com.exemple.facilita.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.CriarPrestadorRequest
import com.exemple.facilita.model.LocalizacaoRequest
import com.exemple.facilita.model.LocalizacaoResponse
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrestadorViewModel : ViewModel() {

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()

    private val _sucesso = MutableStateFlow(false)
    val sucesso = _sucesso.asStateFlow()

    private val _novoToken = MutableStateFlow<String?>(null)
    val novoToken = _novoToken.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estados para localização
    private val _localizacoesIds = MutableStateFlow<List<Int>>(emptyList())
    val localizacoesIds = _localizacoesIds.asStateFlow()

    private val _etapaAtual = MutableStateFlow(1) // 1 = endereços, 2 = documentos
    val etapaAtual = _etapaAtual.asStateFlow()

    private val _prestadorCriado = MutableStateFlow(false)
    val prestadorCriado = _prestadorCriado.asStateFlow()

    // Estados para controlar ordem dos documentos
    private val _documentoCadastrado = MutableStateFlow(false)
    val documentoCadastrado = _documentoCadastrado.asStateFlow()

    private val _cnhCadastrada = MutableStateFlow(false)
    val cnhCadastrada = _cnhCadastrada.asStateFlow()

    private val _veiculoCadastrado = MutableStateFlow(false)
    val veiculoCadastrado = _veiculoCadastrado.asStateFlow()

    /**
     * Etapa 1: Criar localização e retornar o ID
     */
    fun criarLocalizacao(
        logradouro: String,
        numero: String,
        bairro: String,
        cidade: String,
        cep: String,
        latitude: Double,
        longitude: Double,
        onSuccess: (Int) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("LOCALIZACAO_DEBUG", "Criando localização")
                Log.d("LOCALIZACAO_DEBUG", "Endereço: $logradouro, $numero - $bairro, $cidade")

                val service = RetrofitFactory.userService
                val body = LocalizacaoRequest(
                    logradouro = logradouro,
                    numero = numero,
                    bairro = bairro,
                    cidade = cidade,
                    cep = cep,
                    latitude = latitude,
                    longitude = longitude
                )

                val response = service.criarLocalizacao(body)

                if (response.isSuccessful) {
                    val localizacao = response.body()
                    Log.d("LOCALIZACAO_DEBUG", "Localização criada com ID: ${localizacao?.id}")

                    localizacao?.let {
                        val novaLista = _localizacoesIds.value + it.id
                        _localizacoesIds.value = novaLista
                        onSuccess(it.id)
                        _mensagem.value = "Endereço cadastrado com sucesso!"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LOCALIZACAO_ERROR", "Erro HTTP ${response.code()}: $errorBody")
                    _mensagem.value = "Erro ao cadastrar endereço: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LOCALIZACAO_ERROR", "Erro: ${e.message}", e)
                _mensagem.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Etapa 2: Criar prestador com os IDs de localização
     */
    fun criarPrestador(token: String, idsLocalizacao: List<Int>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("PRESTADOR_DEBUG", "Iniciando criação de prestador")
                Log.d("PRESTADOR_DEBUG", "Token: ${token.take(20)}...")
                Log.d("PRESTADOR_DEBUG", "IDs de Localização: $idsLocalizacao")

                val service = RetrofitFactory.userService
                val body = CriarPrestadorRequest(localizacao = idsLocalizacao)

                val response = service.criarPrestador("Bearer $token", body)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("PRESTADOR_DEBUG", "Resposta: ${responseBody?.message}")
                    Log.d("PRESTADOR_DEBUG", "Novo token recebido: ${responseBody?.token?.take(20)}...")

                    _mensagem.value = responseBody?.message ?: "Prestador criado com sucesso!"
                    _novoToken.value = responseBody?.token
                    _sucesso.value = true
                    _prestadorCriado.value = true
                    _etapaAtual.value = 2 // Avança para etapa de documentos
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PRESTADOR_ERROR", "Erro HTTP ${response.code()}: $errorBody")
                    _mensagem.value = "Erro ao criar prestador: ${response.code()}"
                    _sucesso.value = false
                }
            } catch (e: Exception) {
                Log.e("PRESTADOR_ERROR", "Erro: ${e.message}", e)
                _mensagem.value = "Erro: ${e.message}"
                _sucesso.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _mensagem.value = null
        _sucesso.value = false
        _novoToken.value = null
        _isLoading.value = false
    }

    fun resetLocalizacoes() {
        _localizacoesIds.value = emptyList()
        _etapaAtual.value = 1
        _prestadorCriado.value = false
    }

    fun marcarDocumentoCadastrado() {
        _documentoCadastrado.value = true
    }

    fun marcarCnhCadastrada() {
        _cnhCadastrada.value = true
    }

    fun marcarVeiculoCadastrado() {
        _veiculoCadastrado.value = true
    }
}

