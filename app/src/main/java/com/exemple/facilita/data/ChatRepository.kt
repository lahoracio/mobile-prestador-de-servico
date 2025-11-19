package com.exemple.facilita.data

import android.content.Context
import android.content.SharedPreferences
import com.exemple.facilita.model.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gerenciador de persistência de mensagens do chat
 * Salva mensagens localmente para não perder ao sair da tela
 */
class ChatRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "chat_messages",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    /**
     * Salva as mensagens de um serviço
     */
    fun saveMessages(servicoId: Int, messages: List<ChatMessage>) {
        val json = gson.toJson(messages)
        prefs.edit().putString("messages_$servicoId", json).apply()
    }

    /**
     * Carrega as mensagens de um serviço
     */
    fun loadMessages(servicoId: Int): List<ChatMessage> {
        val json = prefs.getString("messages_$servicoId", null) ?: return emptyList()
        val type = object : TypeToken<List<ChatMessage>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Adiciona uma mensagem às existentes
     */
    fun addMessage(servicoId: Int, message: ChatMessage) {
        val currentMessages = loadMessages(servicoId).toMutableList()
        currentMessages.add(message)
        saveMessages(servicoId, currentMessages)
    }

    /**
     * Limpa as mensagens de um serviço
     */
    fun clearMessages(servicoId: Int) {
        prefs.edit().remove("messages_$servicoId").apply()
    }
}

