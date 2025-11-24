package com.exemple.facilita.websocket

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
                errorCallback?.invoke("Erro ao conectar: $error")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "🔌 Socket desconectado - Reconexão automática ativa...")
            }

            socket?.on("receive_message") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "📥 Mensagem recebida: $data")

                    val message = ChatMessage(
                        servicoId = data.getInt("servicoId"),
                        mensagem = data.getString("mensagem"),
                        sender = data.getString("sender"),
                        userName = data.optString("userName", "Usuário"),
                        timestamp = data.optString("timestamp", "")
                    )
                    messageCallback?.invoke(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erro ao processar mensagem recebida: ${e.message}")
                    errorCallback?.invoke("Erro ao processar mensagem: ${e.message}")
                }
            }

            socket?.on("message_sent") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "✅ Confirmação de mensagem enviada: $data")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar confirmação: ${e.message}")
                }
            }

            socket?.on("error") { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Erro desconhecido do servidor"
                Log.e(TAG, "❌ Erro do servidor: $error")
                errorCallback?.invoke(error)
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
        Log.d(TAG, "📤 Tentando enviar mensagem...")
        Log.d(TAG, "   Socket conectado? ${socket?.connected()}")
        Log.d(TAG, "   ServiceId: $servicoId, TargetUserId: $targetUserId")
        Log.d(TAG, "   Mensagem: $mensagem")

        if (socket?.connected() == true) {
            try {
                val payload = JSONObject().apply {
                    put("servicoId", servicoId)
                    put("mensagem", mensagem)
                    put("sender", currentUserType ?: "prestador")
                    put("targetUserId", targetUserId)
                }

                Log.d(TAG, "📤 Emitindo send_message com payload: $payload")
                socket?.emit("send_message", payload)
                Log.d(TAG, "✅ Mensagem enviada com sucesso!")
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = "Erro ao enviar mensagem: ${e.message}"
                Log.e(TAG, errorMsg, e)
                onError(errorMsg)
            }
        } else {
            val errorMsg = "Socket não está conectado (conectado=${socket?.connected()})"
            Log.e(TAG, "❌ $errorMsg")
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
        Log.d(TAG, "✅ Socket desconectado e limpo")
    }

    fun isConnected(): Boolean {
        val connected = socket?.connected() ?: false
        Log.d(TAG, "🔍 Status de conexão consultado: $connected")
        return connected
    }
}

