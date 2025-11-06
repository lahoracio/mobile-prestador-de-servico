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
                val service = RetrofitFactory().getCNHService()

                val body = CNHRequest(
                    numero_cnh = numeroCNH,
                    categoria = categoria,
                    validade = validade,
                    possui_ear = possuiEAR
                )

                val response = service.cadastrarCNH("Bearer $token", body)

                _mensagem.value = "CNH cadastrada com sucesso!"
                _cnhValidada.value = true
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _mensagem.value = "Erro ao cadastrar CNH: ${errorBody ?: e.message()}"
                _cnhValidada.value = false
            } catch (e: java.io.IOException) {
                _mensagem.value = "Erro de conexão. Verifique sua internet."
                _cnhValidada.value = false
            } catch (e: Exception) {
                _mensagem.value = "Erro inesperado: ${e.message}"
                _cnhValidada.value = false
            }
        }
    }
}
