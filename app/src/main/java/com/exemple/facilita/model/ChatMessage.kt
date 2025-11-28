package com.exemple.facilita.model

data class ChatMessage(
    val id: String = "",
    val servicoId: Int,
    val mensagem: String,
    val sender: String, // "contratante" ou "prestador"
    val senderUserId: Int,
    val senderName: String,
    val senderPhoto: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    SYSTEM,
    LOCATION,
    IMAGE
}

data class UserInfo(
    val userId: Int,
    val userName: String,
    val userType: String, // "contratante" ou "prestador"
    val userPhoto: String? = null
)

data class ChatSocketEvent(
    val servicoId: Int,
    val mensagem: String,
    val sender: String,
    val targetUserId: Int,
    val senderName: String? = null,
    val senderPhoto: String? = null
)

data class TypingIndicator(
    val isTyping: Boolean = false,
    val userName: String = ""
)

