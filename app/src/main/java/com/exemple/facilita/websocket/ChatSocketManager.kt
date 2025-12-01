package com.exemple.facilita.websocket

import android.util.Log
import com.exemple.facilita.model.ChatMessage
import com.exemple.facilita.model.UserInfo
import com.exemple.facilita.util.ChatConfig
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
    }

    private var socket: Socket? = null
    private val gson = Gson()

    private val socketUrl: String
        get() = ChatConfig.SOCKET_URL

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

            val url = socketUrl
            Log.d(TAG, "Conectando ao socket: $url")
            socket = IO.socket(url, opts)

            setupSocketListeners()
            socket?.connect()

            Log.d(TAG, "Tentando conectar ao servidor: $socketUrl")

            // Listener genérico para debug - captura TODOS os eventos
            socket?.on("*") { args ->
                try {
                    Log.d(TAG, "🔔 Evento genérico recebido com ${args.size} argumentos")
                    args.forEachIndexed { index, arg ->
                        Log.d(TAG, "🔔 Argumento $index: $arg")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro no listener genérico: ${e.message}")
                }
            }
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

            // Evento: Receber mensagem (conforme documentação -> receive_message)
            on("receive_message") { args ->
                Log.d(TAG, "📩 Evento 'receive_message' recebido (principal)")
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Payload completo: $data")
                    processIncomingMessage(data)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erro ao processar evento 'receive_message': ${e.message}", e)
                    e.printStackTrace()
                }
            }

            // Evento: Usuário está digitando (user_typing)
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

            // Evento: Mensagem enviada com sucesso (opcional)
            on("message_sent") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "✅ Confirmação de envio recebida: $data")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar confirmação de envio: ${e.message}")
                }
            }

            // Eventos alternativos que o servidor pode usar (fallback)
            on("message") { args ->
                Log.d(TAG, "📩 Evento 'message' recebido (alternativo)")
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Payload: $data")
                    // Processar igual ao receive_message
                    processIncomingMessage(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar evento 'message': ${e.message}", e)
                }
            }

            on("chat_message") { args ->
                Log.d(TAG, "📩 Evento 'chat_message' recebido (alternativo)")
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Payload: $data")
                    processIncomingMessage(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar evento 'chat_message': ${e.message}", e)
                }
            }

            on("new_message") { args ->
                Log.d(TAG, "📩 Evento 'new_message' recebido (alternativo)")
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📩 Payload: $data")
                    processIncomingMessage(data)
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar evento 'new_message': ${e.message}", e)
                }
            }
        }
    }

    /**
     * Processa mensagem recebida (método auxiliar para evitar duplicação de código)
     */
    private fun processIncomingMessage(data: JSONObject) {
        try {
            val servicoId = data.optInt("servicoId")
            val mensagem = data.optString("mensagem", "")
            val sender = data.optString("sender", "")
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())

            Log.d(TAG, "📩 Processando: servicoId=$servicoId, sender=$sender, mensagem=$mensagem")

            // Extrair informações do usuário (tentar múltiplas fontes)
            val userInfo = data.optJSONObject("userInfo")
            val senderName = userInfo?.optString("userName")
                ?: data.optString("senderName")
                ?: data.optString("userName", "Usuário")
            val senderUserId = userInfo?.optInt("userId")
                ?: data.optInt("userId")
                ?: data.optInt("senderId", 0)
            val senderPhoto = userInfo?.optString("userPhoto")
                ?: data.optString("senderPhoto")

            Log.d(TAG, "📩 Dados extraídos: userName=$senderName, userId=$senderUserId")

            // Verificar se mensagem já existe (para evitar duplicação)
            val currentMessages = _messages.value
            val isDuplicate = currentMessages.any { existingMsg ->
                existingMsg.mensagem == mensagem &&
                existingMsg.sender == sender &&
                // Tolerância de 2 segundos no timestamp para considerar duplicata
                Math.abs(existingMsg.timestamp - timestamp) < 2000
            }

            if (isDuplicate) {
                Log.w(TAG, "⚠️ Mensagem duplicada detectada e ignorada: '$mensagem'")
                return
            }

            val chatMessage = ChatMessage(
                id = "${timestamp}_${senderUserId}_${sender}",
                servicoId = servicoId,
                mensagem = mensagem,
                sender = sender,
                senderUserId = senderUserId,
                senderName = senderName,
                senderPhoto = senderPhoto,
                timestamp = timestamp
            )

            // Adiciona a mensagem à lista
            val updatedMessages = currentMessages.toMutableList()
            updatedMessages.add(chatMessage)
            _messages.value = updatedMessages

            Log.d(TAG, "✅ Mensagem processada e adicionada: '${chatMessage.mensagem}' de ${chatMessage.senderName} (${chatMessage.sender})")
            Log.d(TAG, "✅ Total de mensagens: ${updatedMessages.size}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao processar mensagem: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * Registra o usuário conectado (user_connected)
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
     * Entra na sala do serviço (join_servico)
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
     * Envia mensagem para o chat (send_message)
     */
    fun sendMessage(
        servicoId: Int,
        mensagem: String,
        sender: String,
        targetUserId: Int,
        senderName: String,
        senderPhoto: String? = null,
        senderUserId: Int = 0
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
            Log.d(TAG, "📤 Enviando mensagem para servicoId=$servicoId, sender=$sender, target=$targetUserId")
            Log.d(TAG, "📤 Conteúdo: $mensagem")

            // Adiciona a mensagem localmente (otimista) - com o userId correto do remetente
            val chatMessage = ChatMessage(
                id = "${System.currentTimeMillis()}_local",
                servicoId = servicoId,
                mensagem = mensagem,
                sender = sender,
                senderUserId = senderUserId, // Corrigido: usar senderUserId em vez de targetUserId
                senderName = senderName,
                senderPhoto = senderPhoto,
                timestamp = System.currentTimeMillis()
            )

            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(chatMessage)
            _messages.value = currentMessages

            Log.d(TAG, "✅ Mensagem adicionada localmente: sender=$sender, userId=$senderUserId")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar mensagem: ${e.message}", e)
            _errorMessage.value = "Erro ao enviar mensagem: ${e.message}"
        }
    }

    /**
     * Indica que o usuário está digitando (user_typing)
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
     * Sai da sala do serviço (leave_servico)
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
