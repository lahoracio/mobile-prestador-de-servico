package com.exemple.facilita.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.exemple.facilita.model.ChatMessage
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class ChatSocketManager private constructor() {

    companion object {
        private const val TAG = "ChatSocketManager"
        // URL correta do servidor WebSocket no Azure
        private const val SOCKET_URL = "wss://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net"

        @Volatile
        private var instance: ChatSocketManager? = null

        fun getInstance(): ChatSocketManager {
            return instance ?: synchronized(this) {
                instance ?: ChatSocketManager().also { instance = it }
            }
        }
    }

    private var socket: Socket? = null
    private var currentUserId: Int? = null
    private var currentUserType: String? = null
    private var currentUserName: String? = null
    private var currentServiceId: Int? = null
    private var messageCallback: ((ChatMessage) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun connect(
        userId: Int,
        userType: String,
        userName: String,
        servicoId: Int,
        onMessageReceived: (ChatMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        currentUserId = userId
        currentUserType = userType
        currentUserName = userName
        currentServiceId = servicoId
        messageCallback = onMessageReceived
        errorCallback = onError

        // Se já está conectado, apenas entrar na sala
        if (socket?.connected() == true) {
            Log.d(TAG, "✅ Já conectado! Apenas entrando na sala do serviço: $servicoId")
            socket?.emit("join_servico", servicoId.toString())
            return
        }

        try {
            Log.d(TAG, "🔧 Configurando Socket.IO...")
            Log.d(TAG, "   URL: $SOCKET_URL")
            Log.d(TAG, "   UserId: $userId")
            Log.d(TAG, "   UserType: $userType")
            Log.d(TAG, "   UserName: $userName")
            Log.d(TAG, "   ServicoId: $servicoId")

            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 10000
            }

            socket = IO.socket(SOCKET_URL, options)
            Log.d(TAG, "✅ Socket criado com sucesso")

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket conectado com sucesso")

                // Registrar usuário conectado
                val userData = JSONObject().apply {
                    put("userId", currentUserId)
                    put("userType", currentUserType)
                    put("userName", currentUserName)
                }
                socket?.emit("user_connected", userData)
                Log.d(TAG, "📤 Evento user_connected enviado: $userData")

                // Entrar na sala do serviço
                socket?.emit("join_servico", currentServiceId.toString())
                Log.d(TAG, "🔗 Entrou na sala do serviço: $currentServiceId")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Erro desconhecido"
                Log.e(TAG, "❌ Erro ao conectar: $error")
                mainHandler.post {
                    errorCallback?.invoke("Erro ao conectar: $error")
                }
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "🔌 Socket desconectado - Reconexão automática ativa...")
            }

            socket?.on("receive_message") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "📥 MENSAGEM RECEBIDA DO SERVIDOR")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, data.toString(2))
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "")

                    val message = ChatMessage(
                        servicoId = data.getInt("servicoId"),
                        mensagem = data.getString("mensagem"),
                        sender = data.getString("sender"),
                        userName = data.optString("userName", "Usuário"),
                        timestamp = data.optString("timestamp", "")
                    )

                    Log.d(TAG, "📨 Entregando mensagem para UI:")
                    Log.d(TAG, "   Sender: ${message.sender}")
                    Log.d(TAG, "   UserName: ${message.userName}")
                    Log.d(TAG, "   Mensagem: ${message.mensagem}")

                    // Executar callback na Main Thread
                    mainHandler.post {
                        messageCallback?.invoke(message)
                        Log.d(TAG, "✅ Mensagem entregue ao callback na Main Thread")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erro ao processar mensagem recebida: ${e.message}")
                    e.printStackTrace()
                    mainHandler.post {
                        errorCallback?.invoke("Erro ao processar mensagem: ${e.message}")
                    }
                }
            }

            socket?.on("message_sent") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "✅ CONFIRMAÇÃO: MENSAGEM ENVIADA COM SUCESSO")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, data.toString(2))
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar confirmação: ${e.message}")
                }
            }

            // Listener para qualquer evento genérico (debug)
            socket?.on("message") { args ->
                Log.d(TAG, "📬 Evento genérico 'message': ${args.joinToString()}")
            }

            // Listener para broadcast de mensagens (evento que o servidor realmente envia)
            socket?.on("new_message") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "📣 BROADCAST: NOVA MENSAGEM")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, data.toString(2))
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "")

                    // Processar e entregar para UI
                    val senderInfo = data.optJSONObject("senderInfo")
                    val message = ChatMessage(
                        servicoId = data.getInt("servicoId"),
                        mensagem = data.getString("mensagem"),
                        sender = data.getString("sender"),
                        userName = senderInfo?.optString("userName") ?: "Usuário",
                        timestamp = data.optString("timestamp", "")
                    )

                    Log.d(TAG, "📨 Processando broadcast para UI:")
                    Log.d(TAG, "   Sender: ${message.sender}")
                    Log.d(TAG, "   UserName: ${message.userName}")
                    Log.d(TAG, "   Mensagem: ${message.mensagem}")

                    // Executar callback na Main Thread
                    mainHandler.post {
                        messageCallback?.invoke(message)
                        Log.d(TAG, "✅ Broadcast entregue ao callback na Main Thread")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erro ao processar new_message: ${e.message}")
                    e.printStackTrace()
                }
            }

            socket?.on("error") { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Erro desconhecido do servidor"
                Log.e(TAG, "❌ Erro do servidor: $error")
                mainHandler.post {
                    errorCallback?.invoke(error)
                }
            }

            socket?.connect()
            Log.d(TAG, "🔌 Tentando conectar ao servidor WebSocket...")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "❌ Erro na URI do socket: ${e.message}")
            errorCallback?.invoke("Erro na configuração do servidor: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao criar socket: ${e.message}")
            errorCallback?.invoke("Erro ao criar conexão: ${e.message}")
        }
    }

    fun sendMessage(servicoId: Int, mensagem: String, targetUserId: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        Log.d(TAG, "")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📤 ENVIANDO MENSAGEM VIA WEBSOCKET")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔍 Socket conectado? ${socket?.connected()}")
        Log.d(TAG, "🔍 Socket existe? ${socket != null}")
        Log.d(TAG, "📋 ServiceId: $servicoId")
        Log.d(TAG, "👤 TargetUserId: $targetUserId")
        Log.d(TAG, "👤 CurrentUserId: $currentUserId")
        Log.d(TAG, "📝 Sender: ${currentUserType ?: "prestador"}")
        Log.d(TAG, "💬 Mensagem: \"$mensagem\"")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        if (socket?.connected() == true) {
            try {
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())

                val payload = JSONObject().apply {
                    put("servicoId", servicoId)
                    put("mensagem", mensagem)
                    put("sender", currentUserType ?: "prestador")
                    put("targetUserId", targetUserId)
                    put("userId", currentUserId)
                    put("userName", currentUserName)
                    put("timestamp", timestamp)
                }

                Log.d(TAG, "📦 Payload completo:")
                Log.d(TAG, payload.toString(2))
                Log.d(TAG, "")
                Log.d(TAG, "🚀 Emitindo evento 'send_message'...")

                // Emitir evento
                socket?.emit("send_message", payload)

                Log.d(TAG, "✅ socket.emit() executado!")
                Log.d(TAG, "⏳ Aguardando confirmação do servidor...")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "")

                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Erro ao enviar mensagem: ${e.message}"
                Log.e(TAG, "")
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e(TAG, "❌ ERRO AO ENVIAR MENSAGEM")
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e(TAG, errorMsg, e)
                Log.e(TAG, "Stack trace:")
                e.printStackTrace()
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e(TAG, "")
                onError(errorMsg)
            }
        } else {
            val errorMsg = "Socket não está conectado (conectado=${socket?.connected()})"
            Log.e(TAG, "")
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.e(TAG, "❌ SOCKET NÃO CONECTADO")
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.e(TAG, errorMsg)
            Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.e(TAG, "")
            onError(errorMsg)
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔴 Desconectando socket...")
        socket?.disconnect()
        socket?.off()
        socket = null
        currentUserId = null
        currentUserType = null
        currentUserName = null
        currentServiceId = null
        messageCallback = null
        errorCallback = null
        mainHandler.removeCallbacksAndMessages(null)
        Log.d(TAG, "✅ Socket desconectado e limpo")
    }

    fun isConnected(): Boolean {
        val connected = socket?.connected() ?: false
        Log.d(TAG, "🔍 Status de conexão consultado: $connected")
        return connected
    }
}

