package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.ServicoDetalhe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicoState(
    val isLoading: Boolean = false,
    val servico: ServicoDetalhe? = null,
    val error: String? = null
)

class ServicoViewModel : ViewModel() {
    private val _servicoState = MutableStateFlow(ServicoState())
    val servicoState: StateFlow<ServicoState> = _servicoState.asStateFlow()

    private val _servicosAceitos = MutableStateFlow<Map<Int, ServicoDetalhe>>(emptyMap())
    val servicosAceitos: StateFlow<Map<Int, ServicoDetalhe>> = _servicosAceitos.asStateFlow()

    fun carregarServico(servicoId: Int) {
        viewModelScope.launch {
            _servicoState.value = _servicoState.value.copy(isLoading = true)

            try {
                // Buscar o serviço aceito no cache
                val servico = _servicosAceitos.value[servicoId]

                if (servico != null) {
                    _servicoState.value = ServicoState(
                        isLoading = false,
                        servico = servico,
                        error = null
                    )
                } else {
                    _servicoState.value = ServicoState(
                        isLoading = false,
                        servico = null,
                        error = "Serviço não encontrado"
                    )
                }
            } catch (e: Exception) {
                _servicoState.value = ServicoState(
                    isLoading = false,
                    servico = null,
                    error = e.message ?: "Erro ao carregar serviço"
                )
            }
        }
    }

    fun salvarServicoAceito(servicoDetalhe: ServicoDetalhe) {
        viewModelScope.launch {
            val novosServicos = _servicosAceitos.value.toMutableMap()
            novosServicos[servicoDetalhe.id] = servicoDetalhe
            _servicosAceitos.value = novosServicos
        }
    }

    fun limparEstado() {
        _servicoState.value = ServicoState()
    }
}

