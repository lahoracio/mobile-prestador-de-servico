package com.exemple.facilita.service

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Serviço de WebSocket para rastreamento em tempo real
 * Baseado na documentação: wss://servidor-facilita.onrender.com
 */
class WebSocketService {

    companion object {
        private const val TAG = "WebSocketService"
        private const val SOCKET_URL = "https://servidor-facilita.onrender.com"

        @Volatile
        private var instance: WebSocketService? = null

        fun getInstance(): WebSocketService {
            return instance ?: synchronized(this) {
                instance ?: WebSocketService().also { instance = it }
            }
        }
    }

    private var socket: Socket? = null

    // Estados de conexão
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationUpdate?>(null)
    val currentLocation: StateFlow<LocationUpdate?> = _currentLocation.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String>("Desconectado")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // Dados da sessão
    private var currentUserId: Int? = null
    private var currentUserType: String? = null
    private var currentServiceId: Int? = null

    init {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 20000
            }

            socket = IO.socket(SOCKET_URL, options)
            setupSocketListeners()

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Erro ao criar socket", e)
        }
    }

    private fun setupSocketListeners() {
        socket?.apply {
            // Conexão estabelecida
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Conectado ao servidor WebSocket")
                _isConnected.value = true
                _connectionStatus.value = "Conectado"

                // Reautenticar se já tinha dados
                currentUserId?.let { userId ->
                    currentUserType?.let { userType ->
                        authenticateUser(userId, userType, "")
                    }
                }
            }

            // Desconexão
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Desconectado do servidor")
                _isConnected.value = false
                _connectionStatus.value = "Desconectado"
            }

            // Erro de conexão
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "❌ Erro de conexão: ${args.contentToString()}")
                _connectionStatus.value = "Erro de conexão"
            }

            // Resposta de user_connected
            on("user_connected") { args ->
                val data = args[0] as JSONObject
                Log.d(TAG, "👤 Usuário autenticado: $data")
                _connectionStatus.value = "Autenticado"
            }

            // Resposta de join_servico
            on("servico_joined") { args ->
                val data = args[0] as JSONObject
                Log.d(TAG, "🔗 Conectado ao serviço: $data")
                _connectionStatus.value = "Na sala do serviço"
            }

            // Receber atualizações de localização
            on("location_updated") { args ->
                val data = args[0] as JSONObject
                Log.d(TAG, "📍 Localização atualizada: $data")

                try {
                    val locationUpdate = LocationUpdate(
                        servicoId = data.getInt("servicoId"),
                        latitude = data.getDouble("latitude"),
                        longitude = data.getDouble("longitude"),
                        userId = data.optInt("userId", 0),
                        prestadorName = data.optString("prestadorName", ""),
                        timestamp = data.optString("timestamp", "")
                    )
                    _currentLocation.value = locationUpdate
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao processar location_updated", e)
                }
            }

            // Reconexão
            on("reconnect") {
                Log.d(TAG, "🔄 Reconectado ao servidor")
                _connectionStatus.value = "Reconectado"
            }

            // Tentativa de reconexão
            on("reconnecting") { args ->
                val attempt = if (args.isNotEmpty()) args[0] else 0
                Log.d(TAG, "🔄 Tentando reconectar... (tentativa $attempt)")
                _connectionStatus.value = "Reconectando..."
            }

            // Falha na reconexão
            on("reconnect_failed") {
                Log.e(TAG, "❌ Falha ao reconectar")
                _connectionStatus.value = "Erro de reconexão"
            }
        }
    }

    /**
     * Conectar ao servidor WebSocket
     */
    fun connect() {
        if (socket?.connected() == false) {
            Log.d(TAG, "🔌 Conectando ao servidor...")
            _connectionStatus.value = "Conectando..."
            socket?.connect()
        }
    }

    /**
     * Desconectar do servidor
     */
    fun disconnect() {
        Log.d(TAG, "🔌 Desconectando...")
        socket?.disconnect()
        _isConnected.value = false
        _connectionStatus.value = "Desconectado"
    }

    /**
     * Autenticar usuário no WebSocket
     * Evento: user_connected
     */
    fun authenticateUser(userId: Int, userType: String, userName: String) {
        currentUserId = userId
        currentUserType = userType

        val data = JSONObject().apply {
            put("userId", userId)
            put("userType", userType)
            put("userName", userName)
        }

        Log.d(TAG, "📤 Enviando user_connected: $data")
        socket?.emit("user_connected", data)
    }

    /**
     * Entrar na sala do serviço
     * Evento: join_servico
     */
    fun joinServico(servicoId: Int) {
        currentServiceId = servicoId

        Log.d(TAG, "📤 Enviando join_servico: $servicoId")
        socket?.emit("join_servico", servicoId.toString())
    }

    /**
     * Enviar atualização de localização
     * Evento: update_location
     */
    fun updateLocation(servicoId: Int, latitude: Double, longitude: Double, userId: Int) {
        if (!_isConnected.value) {
            Log.w(TAG, "⚠️ Tentando enviar localização sem estar conectado")
            return
        }

        val data = JSONObject().apply {
            put("servicoId", servicoId)
            put("latitude", latitude)
            put("longitude", longitude)
            put("userId", userId)
        }

        Log.d(TAG, "📤 Enviando update_location: lat=$latitude, lng=$longitude")
        socket?.emit("update_location", data)
    }

    /**
     * Sair da sala do serviço
     */
    fun leaveServico(servicoId: Int) {
        Log.d(TAG, "🚪 Saindo do serviço: $servicoId")
        socket?.emit("leave_servico", servicoId.toString())
        currentServiceId = null
    }

    /**
     * Limpar recursos
     */
    fun cleanup() {
        currentServiceId?.let { leaveServico(it) }
        disconnect()
        socket?.off()
    }
}

/**
 * Modelo de atualização de localização
 */
data class LocationUpdate(
    val servicoId: Int,
    val latitude: Double,
    val longitude: Double,
    val userId: Int = 0,
    val prestadorName: String = "",
    val timestamp: String = ""
)

