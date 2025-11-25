package com.exemple.facilita.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.service.LocationService
import com.exemple.facilita.service.LocationUpdate
import com.exemple.facilita.service.WebSocketLocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar rastreamento em tempo real
 */
class RastreamentoViewModel : ViewModel() {

    companion object {
        private const val TAG = "RastreamentoViewModel"
    }

    private val webSocketService = WebSocketLocationService.getInstance()
    private var locationService: LocationService? = null
    private var locationJob: Job? = null

    // Estado da conexão WebSocket
    val isConnected: StateFlow<Boolean> = webSocketService.isConnected
    val connectionStatus: StateFlow<String> = webSocketService.connectionStatus

    // Localização atual do usuário
    private val _myLocation = MutableStateFlow<LatLng?>(null)
    val myLocation: StateFlow<LatLng?> = _myLocation.asStateFlow()

    // Localização do outro usuário (prestador ou contratante)
    private val _otherUserLocation = MutableStateFlow<LatLng?>(null)
    val otherUserLocation: StateFlow<LatLng?> = _otherUserLocation.asStateFlow()

    // Informações da última atualização
    private val _lastUpdate = MutableStateFlow<LocationUpdate?>(null)
    val lastUpdate: StateFlow<LocationUpdate?> = _lastUpdate.asStateFlow()

    // Estado de rastreamento
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Dados da sessão
    private var servicoId: Int? = null
    private var userId: Int? = null

    init {
        // Observar atualizações de localização do WebSocket
        viewModelScope.launch {
            webSocketService.currentLocation.collect { update ->
                update?.let {
                    Log.d(TAG, "📍 Localização recebida: ${it.latitude}, ${it.longitude}")
                    _otherUserLocation.value = LatLng(it.latitude, it.longitude)
                    _lastUpdate.value = it
                }
            }
        }
    }

    /**
     * Inicializa o serviço de localização
     */
    fun initLocationService(context: Context) {
        if (locationService == null) {
            locationService = LocationService(context)
        }
    }

    /**
     * Inicia o rastreamento completo
     * Conecta ao WebSocket e começa a enviar localização
     */
    fun startTracking(
        context: Context,
        servicoId: Int,
        userId: Int,
        userType: String,
        userName: String
    ) {
        if (_isTracking.value) {
            Log.w(TAG, "⚠️ Rastreamento já está ativo")
            return
        }

        this.servicoId = servicoId
        this.userId = userId

        // Inicializar serviço de localização
        initLocationService(context)

        if (locationService?.hasLocationPermission() != true) {
            Log.e(TAG, "❌ Permissão de localização não concedida")
            return
        }

        _isTracking.value = true

        // Conectar ao WebSocket
        webSocketService.connect()

        viewModelScope.launch {
            // Aguardar conexão
            webSocketService.isConnected
                .filter { it }
                .first()

            // Autenticar usuário
            webSocketService.authenticateUser(userId, userType, userName)

            // Entrar na sala do serviço
            kotlinx.coroutines.delay(500)
            webSocketService.joinServico(servicoId)

            // Iniciar envio de localização
            startSendingLocation()
        }
    }

    /**
     * Inicia o envio contínuo de localização
     */
    private fun startSendingLocation() {
        locationJob?.cancel()

        locationJob = viewModelScope.launch {
            locationService?.startLocationUpdates()?.collect { location ->
                // Atualizar localização local
                _myLocation.value = LatLng(location.latitude, location.longitude)

                // Enviar para o servidor via WebSocket
                servicoId?.let { sId ->
                    userId?.let { uId ->
                        webSocketService.updateLocation(
                            servicoId = sId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            userId = uId
                        )
                    }
                }
            }
        }
    }

    /**
     * Para o rastreamento
     */
    fun stopTracking() {
        if (!_isTracking.value) return

        Log.d(TAG, "🛑 Parando rastreamento")

        _isTracking.value = false

        // Parar envio de localização
        locationJob?.cancel()
        locationJob = null

        // Sair da sala do serviço
        servicoId?.let { webSocketService.leaveServico(it) }

        // Parar serviço de localização
        locationService?.stopLocationUpdates()

        // Desconectar WebSocket
        webSocketService.disconnect()

        // Limpar dados
        _myLocation.value = null
        _otherUserLocation.value = null
        _lastUpdate.value = null
    }

    /**
     * Pausa temporariamente o envio de localização
     * (mantém conexão WebSocket ativa)
     */
    fun pauseLocationUpdates() {
        locationJob?.cancel()
        locationService?.stopLocationUpdates()
    }

    /**
     * Resume o envio de localização
     */
    fun resumeLocationUpdates() {
        if (_isTracking.value) {
            startSendingLocation()
        }
    }

    /**
     * Obtém a localização atual uma única vez
     */
    suspend fun getCurrentLocation(context: Context): LatLng? {
        initLocationService(context)
        val location = locationService?.getCurrentLocation()
        return location?.let { LatLng(it.latitude, it.longitude) }
    }

    /**
     * Calcula a distância entre dois pontos em metros
     */
    fun calculateDistance(from: LatLng, to: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0]
    }

    /**
     * Formata distância para exibição
     */
    fun formatDistance(meters: Float): String {
        return when {
            meters < 1000 -> "${meters.toInt()} m"
            else -> "%.1f km".format(meters / 1000)
        }
    }

    /**
     * Limpar recursos ao destruir ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        stopTracking()
        webSocketService.cleanup()
    }
}

