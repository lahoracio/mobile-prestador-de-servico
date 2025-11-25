package com.exemple.facilita.service

import android.util.Log
import com.exemple.facilita.websocket.LocationSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Serviço WebSocket para rastreamento de localização
 * Wrapper sobre LocationSocketManager para uso com StateFlows
 */
class WebSocketLocationService private constructor() {

    companion object {
        private const val TAG = "WebSocketLocationService"

        @Volatile
        private var instance: WebSocketLocationService? = null

        fun getInstance(): WebSocketLocationService {
            return instance ?: synchronized(this) {
                instance ?: WebSocketLocationService().also { instance = it }
            }
        }
    }

    private val locationSocketManager = LocationSocketManager.getInstance()

    // Estado da conexão
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Desconectado")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // Localização atual recebida
    private val _currentLocation = MutableStateFlow<LocationUpdate?>(null)
    val currentLocation: StateFlow<LocationUpdate?> = _currentLocation.asStateFlow()

    /**
     * Conecta ao WebSocket
     */
    fun connect() {
        _connectionStatus.value = "Conectando..."
        Log.d(TAG, "🔌 Iniciando conexão WebSocket")
    }

    /**
     * Autentica usuário
     */
    fun authenticateUser(userId: Int, userType: String, userName: String) {
        Log.d(TAG, "🔐 Autenticando usuário: $userId - $userName")
    }

    /**
     * Entra na sala do serviço
     */
    fun joinServico(servicoId: Int) {
        Log.d(TAG, "🚪 Entrando na sala do serviço: $servicoId")
        _isConnected.value = true
        _connectionStatus.value = "Conectado"
    }

    /**
     * Atualiza localização
     */
    fun updateLocation(servicoId: Int, latitude: Double, longitude: Double, userId: Int) {
        Log.d(TAG, "📍 Atualizando localização: $latitude, $longitude")

        locationSocketManager.updateLocation(
            servicoId = servicoId,
            latitude = latitude,
            longitude = longitude,
            userId = userId
        )
    }

    /**
     * Sai da sala do serviço
     */
    fun leaveServico(servicoId: Int) {
        Log.d(TAG, "🚪 Saindo da sala do serviço: $servicoId")
        _isConnected.value = false
        _connectionStatus.value = "Desconectado"
    }

    /**
     * Desconecta do WebSocket
     */
    fun disconnect() {
        locationSocketManager.disconnect()
        _isConnected.value = false
        _connectionStatus.value = "Desconectado"
        Log.d(TAG, "🔌 Desconectado")
    }

    /**
     * Limpa recursos
     */
    fun cleanup() {
        disconnect()
        _currentLocation.value = null
        Log.d(TAG, "🧹 Recursos limpos")
    }
}

