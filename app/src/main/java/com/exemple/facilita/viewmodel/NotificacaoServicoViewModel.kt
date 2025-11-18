package com.exemple.facilita.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.Servico
import com.exemple.facilita.service.ApiResponse
import com.exemple.facilita.service.RetrofitFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificacaoServicoViewModel(application: Application) : AndroidViewModel(application) {

    private val _novoServico = MutableStateFlow<Servico?>(null)
    val novoServico: StateFlow<Servico?> = _novoServico

    private val _mostrarNotificacao = MutableStateFlow(false)
    val mostrarNotificacao: StateFlow<Boolean> = _mostrarNotificacao

    private val _tempoRestante = MutableStateFlow(10)
    val tempoRestante: StateFlow<Int> = _tempoRestante

    private var ultimoServicoId: Int? = null

    fun iniciarMonitoramento(token: String) {
        viewModelScope.launch {
            while (true) {
                buscarNovoServico(token)
                delay(5000) // Verifica a cada 5 segundos
            }
        }
    }

    private fun buscarNovoServico(token: String) {
        val service = RetrofitFactory.getServicoService()
        val call = service.getServicosDisponiveis(token)

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val servicos = response.body()?.data ?: emptyList()

                // Pega o primeiro serviço que ainda não foi mostrado
                val novoServico = servicos.firstOrNull { it.id != ultimoServicoId }

                if (novoServico != null && !_mostrarNotificacao.value) {
                    _novoServico.value = novoServico
                    ultimoServicoId = novoServico.id
                    mostrarNotificacaoComTimer()
                }
            }
        } catch (e: Exception) {
            // Ignora erros silenciosamente para não atrapalhar a experiência
        }
    }

    private fun mostrarNotificacaoComTimer() {
        viewModelScope.launch {
            _mostrarNotificacao.value = true
            _tempoRestante.value = 10

            // Countdown de 10 segundos
            repeat(10) {
                delay(1000)
                _tempoRestante.value -= 1
            }

            // Fecha automaticamente após 10 segundos
            if (_mostrarNotificacao.value) {
                fecharNotificacao()
            }
        }
    }

    fun fecharNotificacao() {
        _mostrarNotificacao.value = false
        _novoServico.value = null
        _tempoRestante.value = 10
    }

    fun pararMonitoramento() {
        // Cancela o monitoramento quando necessário
        fecharNotificacao()
    }
}

