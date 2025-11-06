package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.DocumentoRequest
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentoViewModel : ViewModel() {

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()

    private val _documentosCadastrados = MutableStateFlow<MutableSet<String>>(mutableSetOf())
    val documentosCadastrados = _documentosCadastrados.asStateFlow()

    fun setMensagem(msg: String) {
        _mensagem.value = msg
    }

    fun cadastrarDocumento(
        token: String,
        tipoDocumento: String,
        valor: String,
        dataValidade: String,
        arquivoUrl: String = "https://exemplo.com/documento.pdf"
    ) {
        viewModelScope.launch {
            try {
                val service = RetrofitFactory().getDocumentoService()

                val body = DocumentoRequest(
                    tipo_documento = tipoDocumento,
                    valor = valor,
                    data_validade = dataValidade,
                    arquivo_url = arquivoUrl
                )

                val response = service.cadastrarDocumento("Bearer $token", body)

                _mensagem.value = "Documento ${tipoDocumento} cadastrado com sucesso!"

                // Adiciona o tipo de documento à lista de cadastrados
                _documentosCadastrados.value = _documentosCadastrados.value.toMutableSet().apply {
                    add(tipoDocumento)
                }

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _mensagem.value = "Erro ao cadastrar ${tipoDocumento}: ${errorBody ?: e.message()}"
            } catch (e: java.io.IOException) {
                _mensagem.value = "Erro de conexão. Verifique sua internet."
            } catch (e: Exception) {
                _mensagem.value = "Erro inesperado: ${e.message}"
            }
        }
    }
}

