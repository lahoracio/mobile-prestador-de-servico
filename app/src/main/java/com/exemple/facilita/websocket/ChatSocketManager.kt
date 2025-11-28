package com.exemple.facilita.websocket

import android.util.Log
import com.exemple.facilita.model.ChatMessage
import com.exemple.facilita.model.UserInfo
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException

class ChatSocketManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: ChatSocketManager? = null

        fun getInstance(): ChatSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatSocketManager().also { INSTANCE = it }
            }
        }

        private const val TAG = "ChatSocketManager"
        private const val SOCKET_URL = "https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net"
    }

    private var socket: Socket? = null
    private val gson = Gson()

    // Estados observáveis
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _typingIndicator = MutableStateFlow<Pair<Boolean, String>>(false to "")
    val typingIndicator: StateFlow<Pair<Boolean, String>> = _typingIndicator.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    /**
     * Inicializa a conexão com o servidor Socket.IO
     */
    fun connect() {
        try {
            if (socket?.connected() == true) {
                Log.d(TAG, "Socket já está conectado")
                return
            }

            _connectionState.value = ConnectionState.CONNECTING

            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 20000
            }

            socket = IO.socket(SOCKET_URL, opts)

            setupSocketListeners()
            socket?.connect()

            Log.d(TAG, "Tentando conectar ao servidor: $SOCKET_URL")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Erro de URI ao conectar: ${e.message}")
            _connectionState.value = ConnectionState.ERROR
            _errorMessage.value = "Erro ao conectar: URL inválida"
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao conectar: ${e.message}")
            _connectionState.value = ConnectionState.ERROR
            _errorMessage.value = "Erro ao conectar: ${e.message}"
        }
    }

    /**
     * Configura os listeners do Socket.IO
     */
    private fun setupSocketListeners() {
        socket?.apply {
            // Evento: Conexão estabelecida
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Conectado ao servidor Socket.IO")
                _connectionState.value = ConnectionState.CONNECTED
                _errorMessage.value = null
            }

            // Evento: Desconexão
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Desconectado do servidor")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            // Evento: Erro de conexão
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                Log.e(TAG, "❌ Erro de conexão: $error")
                _connectionState.value = ConnectionState.ERROR
                _errorMessage.value = "Erro de conexão: ${error?.toString()}"
            }

            // Evento: Receber mensagem
            on("receive_message") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Mensagem recebida: $data")

                    val servicoId = data.optInt("servicoId")
                    val mensagem = data.optString("mensagem", "")
                    val sender = data.optString("sender", "")
                    val timestamp = data.optLong("timestamp", System.currentTimeMillis())

                    // Extrair informações do usuário
                    val userInfo = data.optJSONObject("userInfo")
                    val senderName = userInfo?.optString("userName") ?: "Usuário"
                    val senderUserId = userInfo?.optInt("userId") ?: 0
                    val senderPhoto = userInfo?.optString("userPhoto")

                    val chatMessage = ChatMessage(
                        id = "${System.currentTimeMillis()}_${senderUserId}",
                        servicoId = servicoId,
                        mensagem = mensagem,
                        sender = sender,
                        senderUserId = senderUserId,
                        senderName = senderName,
                        senderPhoto = senderPhoto,
                        timestamp = timestamp
                    )

                    // Adiciona a mensagem à lista
                    val currentMessages = _messages.value.toMutableList()
                    currentMessages.add(chatMessage)
                    _messages.value = currentMessages

                    Log.d(TAG, "✅ Mensagem adicionada: ${chatMessage.mensagem}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar mensagem recebida: ${e.message}")
                }
            }

            // Evento: Usuário está digitando
            on("user_typing") { args ->
                try {
                    val data = args[0] as JSONObject
                    val userName = data.optString("userName", "Usuário")
                    val isTyping = data.optBoolean("isTyping", false)
                    _typingIndicator.value = isTyping to userName
                    Log.d(TAG, "⌨️ $userName está digitando: $isTyping")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar indicador de digitação: ${e.message}")
                }
            }

            // Evento: Erro no servidor
            on("error") { args ->
                val errorMsg = args.firstOrNull()?.toString() ?: "Erro desconhecido"
                Log.e(TAG, "❌ Erro do servidor: $errorMsg")
                _errorMessage.value = errorMsg
            }

            // Evento: Mensagem enviada com sucesso
            on("message_sent") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "✅ Mensagem enviada com sucesso: $data")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar confirmação de envio: ${e.message}")
                }
            }
        }
    }

    /**
     * Registra o usuário conectado
     */
    fun registerUser(userId: Int, userType: String, userName: String) {
        try {
            val userInfo = JSONObject().apply {
                put("userId", userId)
                put("userType", userType)
                put("userName", userName)
            }

            socket?.emit("user_connected", userInfo)
            Log.d(TAG, "👤 Usuário registrado: $userName ($userType)")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar usuário: ${e.message}")
        }
    }

    /**
     * Entra na sala do serviço
     */
    fun joinServico(servicoId: Int) {
        try {
            socket?.emit("join_servico", servicoId.toString())
            Log.d(TAG, "🚪 Entrando na sala do serviço: $servicoId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao entrar na sala: ${e.message}")
        }
    }

    /**
     * Envia mensagem para o chat
     */
    fun sendMessage(
        servicoId: Int,
        mensagem: String,
        sender: String,
        targetUserId: Int,
        senderName: String,
        senderPhoto: String? = null
    ) {
        try {
            val messageData = JSONObject().apply {
                put("servicoId", servicoId)
                put("mensagem", mensagem)
                put("sender", sender)
                put("targetUserId", targetUserId)
                put("senderName", senderName)
                if (senderPhoto != null) {
                    put("senderPhoto", senderPhoto)
                }
                put("timestamp", System.currentTimeMillis())
            }

            socket?.emit("send_message", messageData)
            Log.d(TAG, "📤 Enviando mensagem: $mensagem")

            // Adiciona a mensagem localmente (otimista)
            val chatMessage = ChatMessage(
                id = "${System.currentTimeMillis()}_local",
                servicoId = servicoId,
                mensagem = mensagem,
                sender = sender,
                senderUserId = targetUserId,
                senderName = senderName,
                senderPhoto = senderPhoto,
                timestamp = System.currentTimeMillis()
            )

            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(chatMessage)
            _messages.value = currentMessages
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar mensagem: ${e.message}")
            _errorMessage.value = "Erro ao enviar mensagem: ${e.message}"
        }
    }

    /**
     * Indica que o usuário está digitando
     */
    fun sendTypingIndicator(servicoId: Int, userName: String, isTyping: Boolean) {
        try {
            val typingData = JSONObject().apply {
                put("servicoId", servicoId)
                put("userName", userName)
                put("isTyping", isTyping)
            }

            socket?.emit("user_typing", typingData)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar indicador de digitação: ${e.message}")
        }
    }

    /**
     * Sai da sala do serviço
     */
    fun leaveServico(servicoId: Int) {
        try {
            socket?.emit("leave_servico", servicoId.toString())
            Log.d(TAG, "🚪 Saindo da sala do serviço: $servicoId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sair da sala: ${e.message}")
        }
    }

    /**
     * Limpa as mensagens
     */
    fun clearMessages() {
        _messages.value = emptyList()
    }

    /**
     * Desconecta do servidor
     */
    fun disconnect() {
        try {
            socket?.disconnect()
            socket?.off()
            _connectionState.value = ConnectionState.DISCONNECTED
            _messages.value = emptyList()
            Log.d(TAG, "🔌 Desconectado do servidor")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao desconectar: ${e.message}")
        }
    }

    /**
     * Verifica se está conectado
     */
    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
}

