package com.exemple.facilita.data

import android.content.Context
import android.util.Log
import com.exemple.facilita.model.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository para gerenciar o armazenamento local de mensagens do chat
 */
class ChatRepository(private val context: Context) {

    private val gson = Gson()
    private val TAG = "ChatRepository"

    companion object {
        private const val CHAT_DIR = "chat_messages"
    }

    /**
     * Salva as mensagens de um serviço no armazenamento local
     */
    suspend fun saveMessages(servicoId: Int, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            val chatDir = File(context.filesDir, CHAT_DIR)
            if (!chatDir.exists()) {
                chatDir.mkdirs()
            }

            val file = File(chatDir, "servico_$servicoId.json")
            val json = gson.toJson(messages)
            file.writeText(json)

            Log.d(TAG, "✅ Mensagens salvas para o serviço $servicoId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar mensagens: ${e.message}")
        }
    }

    /**
     * Carrega as mensagens de um serviço do armazenamento local
     */
    suspend fun loadMessages(servicoId: Int): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val chatDir = File(context.filesDir, CHAT_DIR)
            val file = File(chatDir, "servico_$servicoId.json")

            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val messages: List<ChatMessage> = gson.fromJson(json, type)
                Log.d(TAG, "✅ ${messages.size} mensagens carregadas para o serviço $servicoId")
                return@withContext messages
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar mensagens: ${e.message}")
        }

        return@withContext emptyList()
    }

    /**
     * Adiciona uma nova mensagem ao arquivo local
     */
    suspend fun addMessage(servicoId: Int, message: ChatMessage) = withContext(Dispatchers.IO) {
        try {
            val currentMessages = loadMessages(servicoId).toMutableList()
            currentMessages.add(message)
            saveMessages(servicoId, currentMessages)
            Log.d(TAG, "✅ Mensagem adicionada ao serviço $servicoId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar mensagem: ${e.message}")
        }
    }

    /**
     * Deleta todas as mensagens de um serviço
     */
    suspend fun deleteMessages(servicoId: Int) = withContext(Dispatchers.IO) {
        try {
            val chatDir = File(context.filesDir, CHAT_DIR)
            val file = File(chatDir, "servico_$servicoId.json")

            if (file.exists()) {
                file.delete()
                Log.d(TAG, "✅ Mensagens deletadas do serviço $servicoId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar mensagens: ${e.message}")
        }
    }

    /**
     * Deleta todas as mensagens de todos os serviços
     */
    suspend fun deleteAllMessages() = withContext(Dispatchers.IO) {
        try {
            val chatDir = File(context.filesDir, CHAT_DIR)
            if (chatDir.exists()) {
                chatDir.deleteRecursively()
                Log.d(TAG, "✅ Todas as mensagens foram deletadas")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar todas as mensagens: ${e.message}")
        }
    }

    /**
     * Obtém a última mensagem de um serviço
     */
    suspend fun getLastMessage(servicoId: Int): ChatMessage? = withContext(Dispatchers.IO) {
        try {
            val messages = loadMessages(servicoId)
            return@withContext messages.lastOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter última mensagem: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Verifica se existem mensagens para um serviço
     */
    suspend fun hasMessages(servicoId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val chatDir = File(context.filesDir, CHAT_DIR)
            val file = File(chatDir, "servico_$servicoId.json")
            return@withContext file.exists() && file.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar mensagens: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Conta o número de mensagens de um serviço
     */
    suspend fun countMessages(servicoId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val messages = loadMessages(servicoId)
            return@withContext messages.size
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar mensagens: ${e.message}")
            return@withContext 0
        }
    }
}

