package com.exemple.facilita.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.model.ChatMessage
import com.exemple.facilita.websocket.ChatSocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatSocketManager = ChatSocketManager.getInstance()

    private val TAG = "ChatViewModel"

    // Estado das mensagens
    val messages: StateFlow<List<ChatMessage>> = chatSocketManager.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado da conexão
    val connectionState: StateFlow<ChatSocketManager.ConnectionState> =
        chatSocketManager.connectionState
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ChatSocketManager.ConnectionState.DISCONNECTED
            )

    // Indicador de digitação
    val typingIndicator: StateFlow<Pair<Boolean, String>> =
        chatSocketManager.typingIndicator
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false to "")

    // Mensagens de erro
    val errorMessage: StateFlow<String?> = chatSocketManager.errorMessage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Controle de digitação
    private var typingJob: Job? = null
    private var isCurrentlyTyping = false

    // Informações do usuário atual
    private var currentUserId: Int = 0
    private var currentUserName: String = ""
    private var currentUserType: String = ""

    /**
     * Inicializa a conexão e entra na sala do chat
     */
    fun initializeChat(
        servicoId: Int,
        userId: Int,
        userName: String,
        userType: String
    ) {
        currentUserId = userId
        currentUserName = userName
        currentUserType = userType

        viewModelScope.launch {
            try {
                // Conecta ao servidor
                chatSocketManager.connect()

                // Aguarda a conexão
                connectionState.first { it == ChatSocketManager.ConnectionState.CONNECTED }

                // Registra o usuário
                chatSocketManager.registerUser(userId, userType, userName)

                // Entra na sala do serviço
                chatSocketManager.joinServico(servicoId)

                Log.d(TAG, "Chat inicializado para o serviço $servicoId")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao inicializar chat: ${e.message}")
            }
        }
    }

    /**
     * Envia uma mensagem
     */
    fun sendMessage(
        servicoId: Int,
        mensagem: String,
        targetUserId: Int,
        senderPhoto: String? = null
    ) {
        if (mensagem.isBlank()) {
            Log.w(TAG, "Tentativa de enviar mensagem vazia")
            return
        }

        viewModelScope.launch {
            try {
                chatSocketManager.sendMessage(
                    servicoId = servicoId,
                    mensagem = mensagem.trim(),
                    sender = currentUserType,
                    targetUserId = targetUserId,
                    senderName = currentUserName,
                    senderPhoto = senderPhoto
                )

                // Para de indicar que está digitando
                stopTypingIndicator(servicoId)

                Log.d(TAG, "Mensagem enviada: $mensagem")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao enviar mensagem: ${e.message}")
            }
        }
    }

    /**
     * Indica que o usuário está digitando
     */
    fun startTypingIndicator(servicoId: Int) {
        // Cancela o job anterior se existir
        typingJob?.cancel()

        if (!isCurrentlyTyping) {
            isCurrentlyTyping = true
            chatSocketManager.sendTypingIndicator(servicoId, currentUserName, true)
        }

        // Cria um novo job que irá parar o indicador após 2 segundos
        typingJob = viewModelScope.launch {
            delay(2000)
            stopTypingIndicator(servicoId)
        }
    }

    /**
     * Para o indicador de digitação
     */
    fun stopTypingIndicator(servicoId: Int) {
        typingJob?.cancel()
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false
            chatSocketManager.sendTypingIndicator(servicoId, currentUserName, false)
        }
    }

    /**
     * Sai da sala do chat
     */
    fun leaveChat(servicoId: Int) {
        viewModelScope.launch {
            try {
                stopTypingIndicator(servicoId)
                chatSocketManager.leaveServico(servicoId)
                Log.d(TAG, "Saiu do chat do serviço $servicoId")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sair do chat: ${e.message}")
            }
        }
    }

    /**
     * Limpa as mensagens
     */
    fun clearMessages() {
        chatSocketManager.clearMessages()
    }

    /**
     * Desconecta do servidor
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                chatSocketManager.disconnect()
                Log.d(TAG, "Desconectado do servidor de chat")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao desconectar: ${e.message}")
            }
        }
    }

    /**
     * Verifica se está conectado
     */
    fun isConnected(): Boolean {
        return chatSocketManager.isConnected()
    }

    /**
     * Limpa erro
     */
    fun clearError() {
        // O flow já é imutável, então apenas logamos
        Log.d(TAG, "Erro limpo")
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
        Log.d(TAG, "ChatViewModel limpo")
    }
}

