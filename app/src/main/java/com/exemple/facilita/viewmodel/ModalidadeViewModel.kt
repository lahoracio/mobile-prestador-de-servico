package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.ModalidadeRequest
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModalidadeViewModel : ViewModel() {

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()

    private val _modalidadesCadastradas = MutableStateFlow(false)
    val modalidadesCadastradas = _modalidadesCadastradas.asStateFlow()

    fun setMensagem(msg: String) {
        _mensagem.value = msg
    }

    fun cadastrarModalidades(
        token: String,
        modalidades: List<String>
    ) {
        viewModelScope.launch {
            try {
                val service = RetrofitFactory().getModalidadeService()

                // Converte para maiúsculas conforme esperado pela API
                val modalidadesUpper = modalidades.map { it.uppercase() }

                val body = ModalidadeRequest(
                    modalidades = modalidadesUpper
                )

                val response = service.cadastrarModalidades("Bearer $token", body)

                _mensagem.value = "✅ ${response.message}"
                _modalidadesCadastradas.value = true

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _mensagem.value = "❌ Erro HTTP ${e.code()}: ${errorBody ?: e.message()}"
            } catch (e: java.io.IOException) {
                _mensagem.value = "❌ Erro de conexão: Verifique sua internet"
            } catch (e: Exception) {
                _mensagem.value = "❌ Erro: ${e.message}"
            }
        }
    }
}

