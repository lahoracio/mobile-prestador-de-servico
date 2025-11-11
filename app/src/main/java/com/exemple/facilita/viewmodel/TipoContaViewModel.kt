package com.exemple.facilita.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.CriarPrestadorRequest
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

    fun criarPrestador(token: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("PRESTADOR_DEBUG", "Iniciando criação de prestador")
                Log.d("PRESTADOR_DEBUG", "Token: ${token.take(20)}...")
                Log.d("PRESTADOR_DEBUG", "Localização: [$latitude, $longitude]")

                val service = RetrofitFactory.userService
                val body = CriarPrestadorRequest(localizacao = listOf(latitude, longitude))

                val response = service.criarPrestador("Bearer $token", body)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("PRESTADOR_DEBUG", "Resposta: ${responseBody?.message}")
                    Log.d("PRESTADOR_DEBUG", "Novo token recebido: ${responseBody?.token?.take(20)}...")

                    _mensagem.value = responseBody?.message ?: "Prestador criado com sucesso!"
                    _novoToken.value = responseBody?.token
                    _sucesso.value = true
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
}

