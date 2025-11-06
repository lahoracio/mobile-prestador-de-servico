package com.exemple.facilita.viewmodel

import androidx.lifecycle.ViewModel
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
}

