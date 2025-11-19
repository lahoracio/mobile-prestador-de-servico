package com.exemple.facilita.model

/**
 * Modelo de dados para mensagens do chat
 */
data class ChatMessage(
    val servicoId: Int,
    val mensagem: String,
    val sender: String,
    val userName: String,
    val timestamp: String
)

