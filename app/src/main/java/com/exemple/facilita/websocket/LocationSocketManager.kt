package com.exemple.facilita.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Gerenciador de WebSocket para rastreamento de localização em tempo real
 */
class LocationSocketManager private constructor() {

    companion object {
        private const val TAG = "LocationSocketManager"
        private const val SOCKET_URL = "wss://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net"

        @Volatile
        private var instance: LocationSocketManager? = null

        fun getInstance(): LocationSocketManager {
            return instance ?: synchronized(this) {
                instance ?: LocationSocketManager().also { instance = it }
            }
        }
    }

    private var socket: Socket? = null
    private var currentUserId: Int? = null
    private var currentUserType: String? = null
    private var currentUserName: String? = null
    private var currentServiceId: Int? = null
    private var locationCallback: ((Double, Double, String, String) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Conecta ao WebSocket de localização
     */
    fun connect(
        userId: Int,
        userType: String,
        userName: String,
        servicoId: Int,
        onLocationUpdated: (latitude: Double, longitude: Double, userName: String, timestamp: String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentUserId = userId
        currentUserType = userType
        currentUserName = userName
        currentServiceId = servicoId
        locationCallback = onLocationUpdated
        errorCallback = onError

        // Se já conectado, apenas entrar na sala
        if (socket?.connected() == true) {
            Log.d(TAG, "✅ Já conectado! Entrando na sala do serviço: $servicoId")
            socket?.emit("join_servico", servicoId.toString())
            return
        }

        try {
            Log.d(TAG, "🔧 Configurando Socket.IO para localização...")
            Log.d(TAG, "   URL: $SOCKET_URL")
            Log.d(TAG, "   UserId: $userId")
            Log.d(TAG, "   UserType: $userType")
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

            // Evento: Conectado
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket de localização conectado!")

                // Registrar usuário
                val userData = JSONObject().apply {
                    put("userId", currentUserId)
                    put("userType", currentUserType)
                    put("userName", currentUserName)
                }
                socket?.emit("user_connected", userData)
                Log.d(TAG, "📤 user_connected enviado: $userData")

                // Entrar na sala do serviço
                socket?.emit("join_servico", currentServiceId.toString())
                Log.d(TAG, "🔗 join_servico enviado: $currentServiceId")
            }

            // Evento: Erro de conexão
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Erro desconhecido"
                Log.e(TAG, "❌ Erro ao conectar: $error")
                mainHandler.post {
                    errorCallback?.invoke("Erro ao conectar: $error")
                }
            }

            // Evento: Desconectado
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "🔌 Socket desconectado - Reconexão automática ativa...")
            }

            // Evento: Localização atualizada (broadcast do servidor)
            socket?.on("location_updated") { args ->
                try {
                    val data = args[0] as JSONObject
                    Log.d(TAG, "")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, "📍 LOCALIZAÇÃO ATUALIZADA")
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    Log.d(TAG, data.toString(2))
                    Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                    val latitude = data.getDouble("latitude")
                    val longitude = data.getDouble("longitude")
                    val prestadorName = data.optString("prestadorName", "Prestador")
                    val timestamp = data.optString("timestamp", "")

                    Log.d(TAG, "📍 Nova posição:")
                    Log.d(TAG, "   Latitude: $latitude")
                    Log.d(TAG, "   Longitude: $longitude")
                    Log.d(TAG, "   Nome: $prestadorName")
                    Log.d(TAG, "   Timestamp: $timestamp")

                    // Executar callback na Main Thread
                    mainHandler.post {
                        locationCallback?.invoke(latitude, longitude, prestadorName, timestamp)
                        Log.d(TAG, "✅ Callback de localização executado na Main Thread")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erro ao processar location_updated: ${e.message}")
                    e.printStackTrace()
                }
            }

            // Evento: Erro do servidor
            socket?.on("error") { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Erro do servidor"
                Log.e(TAG, "❌ Erro do servidor: $error")
                mainHandler.post {
                    errorCallback?.invoke(error)
                }
            }

            socket?.connect()
            Log.d(TAG, "🔌 Conectando ao WebSocket de localização...")

        } catch (e: URISyntaxException) {
            Log.e(TAG, "❌ Erro na URI: ${e.message}")
            errorCallback?.invoke("Erro na configuração: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao criar socket: ${e.message}")
            errorCallback?.invoke("Erro ao criar conexão: ${e.message}")
        }
    }

    /**
     * Envia atualização de localização
     */
    fun updateLocation(servicoId: Int, latitude: Double, longitude: Double, userId: Int) {
        if (socket?.connected() != true) {
            Log.e(TAG, "❌ Socket não conectado. Não é possível enviar localização.")
            return
        }

        try {
            val payload = JSONObject().apply {
                put("servicoId", servicoId)
                put("latitude", latitude)
                put("longitude", longitude)
                put("userId", userId)
            }

            Log.d(TAG, "📤 Enviando localização:")
            Log.d(TAG, "   Lat: $latitude, Lng: $longitude")
            Log.d(TAG, "   ServicoId: $servicoId, UserId: $userId")

            socket?.emit("update_location", payload)
            Log.d(TAG, "✅ Localização enviada com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar localização: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Verifica se está conectado
     */
    fun isConnected(): Boolean {
        val connected = socket?.connected() ?: false
        Log.d(TAG, "🔍 Status de conexão: $connected")
        return connected
    }

    /**
     * Desconecta do WebSocket
     */
    fun disconnect() {
        Log.d(TAG, "🔴 Desconectando socket de localização...")
        socket?.disconnect()
        socket?.off()
        socket = null
        currentUserId = null
        currentUserType = null
        currentUserName = null
        currentServiceId = null
        locationCallback = null
        errorCallback = null
        mainHandler.removeCallbacksAndMessages(null)
        Log.d(TAG, "✅ Socket desconectado e limpo")
    }
}

