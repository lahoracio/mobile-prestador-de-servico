package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.CNHRequest
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CNHViewModel : ViewModel() {

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()

    private val _cnhValidada = MutableStateFlow(false)
    val cnhValidada = _cnhValidada.asStateFlow()

    fun setMensagem(msg: String) {
        _mensagem.value = msg
    }

    fun resetCnhValidada() {
        _cnhValidada.value = false
    }

    fun validarCNH(
        token: String,
        numeroCNH: String,
        categoria: String,
        validade: String,
        possuiEAR: Boolean
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CNH_DEBUG", "Iniciando cadastro de CNH")
                android.util.Log.d("CNH_DEBUG", "Token recebido (primeiros 20 chars): ${token.take(20)}...")

                val service = RetrofitFactory.getCNHService()

                val body = CNHRequest(
                    numero_cnh = numeroCNH,
                    categoria = categoria,
                    validade = validade,
                    possui_ear = possuiEAR
                )

                android.util.Log.d("CNH_DEBUG", "Enviando request: $body")
                android.util.Log.d("CNH_DEBUG", "Header Authorization: Bearer ${token.take(20)}...")

                val response = service.cadastrarCNH("Bearer $token", body)

                android.util.Log.d("CNH_DEBUG", "Resposta recebida: $response")

                // Verificar se a CNH foi cadastrada com sucesso
                if (response.cnh != null) {
                    _mensagem.value = response.message
                    _cnhValidada.value = true
                } else {
                    // API retornou "cnh": false indicando erro
                    _mensagem.value = response.message
                    _cnhValidada.value = false
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("CNH_ERROR", "Erro HTTP ${e.code()}: $errorBody")

                // Tratamento específico para erro 401 (token inválido/expirado)
                when (e.code()) {
                    401 -> _mensagem.value = "Token expirado ou inválido. Faça login novamente."
                    400 -> _mensagem.value = "Dados inválidos. Verifique as informações da CNH."
                    404 -> _mensagem.value = "Prestador não encontrado. Certifique-se de ter escolhido 'Prestador de Serviço' no tipo de conta."
                    500 -> _mensagem.value = "Erro no servidor. Tente novamente mais tarde."
                    else -> _mensagem.value = "Erro ao cadastrar CNH: ${errorBody ?: e.message()}"
                }
                _cnhValidada.value = false
            } catch (e: java.io.IOException) {
                android.util.Log.e("CNH_ERROR", "Erro de conexão: ${e.message}")
                _mensagem.value = "Erro de conexão. Verifique sua internet."
                _cnhValidada.value = false
            } catch (e: Exception) {
                android.util.Log.e("CNH_ERROR", "Erro inesperado: ${e.message}", e)
                _mensagem.value = "Erro inesperado: ${e.message}"
                _cnhValidada.value = false
            }
        }
    }
}
