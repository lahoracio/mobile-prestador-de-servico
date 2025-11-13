package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.Modalidade
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

    fun setMensagem(msg: String?) {
        _mensagem.value = msg
    }

    fun resetModalidadesCadastradas() {
        _modalidadesCadastradas.value = false
    }

    fun cadastrarModalidades(
        token: String,
        modalidades: List<Modalidade>
    ) {
        viewModelScope.launch {
            try {
                val service = RetrofitFactory.getModalidadeService()

                val body = ModalidadeRequest(
                    modalidades = modalidades
                )

                val response = service.cadastrarModalidades("Bearer $token", body)

                _mensagem.value = "Informações do veículo cadastradas com sucesso!"
                _modalidadesCadastradas.value = true

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _mensagem.value = "Erro ao cadastrar: ${errorBody ?: e.message()}"
                _modalidadesCadastradas.value = false
            } catch (e: java.io.IOException) {
                _mensagem.value = "Erro de conexão. Verifique sua internet."
                _modalidadesCadastradas.value = false
            } catch (e: Exception) {
                _mensagem.value = "Erro inesperado: ${e.message}"
                _modalidadesCadastradas.value = false
            }
        }
    }
}

