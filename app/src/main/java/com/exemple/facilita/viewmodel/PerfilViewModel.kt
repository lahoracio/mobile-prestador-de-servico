package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
import com.exemple.facilita.model.Contratante
import com.exemple.facilita.model.Prestador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel : ViewModel() {

    private val _documentosValidados = MutableStateFlow<MutableSet<String>>(mutableSetOf())
    val documentosValidados = _documentosValidados.asStateFlow()

    fun marcarComoValidado(documento: String) {
        _documentosValidados.value = _documentosValidados.value.toMutableSet().apply {
            add(documento)
        }
    }

    fun verificarValidacao(documento: String): Boolean {
        return _documentosValidados.value.contains(documento)
    }

    data class ProfileResponse(
        val message: String,
        val token: String,
        val prestador: Prestador?, // Pode ser nulo se o usuário for contratante
        val contratante: Contratante? // Pode ser nulo se o usuário for prestador
    )
}

