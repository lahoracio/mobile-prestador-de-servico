package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.*
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CNHViewModel : ViewModel() {

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem = _mensagem.asStateFlow()

    fun validarCNH(
        token: String,
        numeroCNH: String,
        categoria: String,
        validade: String,
        possuiEAR: String
    ) {
        viewModelScope.launch {
            try {
                val service = RetrofitFactory().getCNHService()

                val body = CNHRequest(
                    localizacao = listOf(1),
                    modalidades = listOf("MOTO", "CARRO"),
                    cnh = CNHData(
                        numero_cnh = numeroCNH,
                        categoria = categoria,
                        validade = validade,
                        possui_ear = possuiEAR.equals("sim", ignoreCase = true)
                    ),
                    documento = listOf(
                        DocumentoData("CPF", "12345678901"),
                        DocumentoData("RG", "123456789", arquivo_url = "http://exemplo.com/rg.jpg"),
                        DocumentoData("MODELO_VEICULO", "Honda CG 160"),
                        DocumentoData("ANO_VEICULO", "2023"),
                        DocumentoData("SEGURO_VEICULO", "true"),
                        DocumentoData("CONTATO_EMERGENCIA", "Maria (11) 99999-9999")
                    )
                )

                val response = service.registrarCNH("Bearer $token", body)

                if (response.success == true) {
                    _mensagem.value = "✅ CNH registrada com sucesso!"
                } else {
                    _mensagem.value = "⚠️ Erro: ${response.message ?: "Não foi possível registrar"}"
                }
            } catch (e: Exception) {
                _mensagem.value = "❌ Erro na requisição: ${e.message}"
            }
        }
    }
}
