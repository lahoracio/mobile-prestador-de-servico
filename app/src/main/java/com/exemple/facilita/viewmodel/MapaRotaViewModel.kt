package com.exemple.facilita.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exemple.facilita.service.DirectionsService
import com.exemple.facilita.service.LocationService
import com.exemple.facilita.service.RouteInfo
import com.exemple.facilita.service.RouteStep
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciar mapa com rota
 */
class MapaRotaViewModel : ViewModel() {

    companion object {
        private const val TAG = "MapaRotaViewModel"
    }

    private var directionsService: DirectionsService? = null
    private var locationService: LocationService? = null

    // Estado da rota
    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo.asStateFlow()

    // Localização atual
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    // Estado de carregamento
    private val _isLoadingRoute = MutableStateFlow(false)
    val isLoadingRoute: StateFlow<Boolean> = _isLoadingRoute.asStateFlow()

    // Erro
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Inicializa os serviços
     */
    fun initServices(context: Context, apiKey: String) {
        if (directionsService == null) {
            directionsService = DirectionsService(apiKey)
        }
        if (locationService == null) {
            locationService = LocationService(context)
        }
    }

    /**
     * Busca rota com paradas intermediárias
     */
    fun fetchRouteWithWaypoints(origin: LatLng, waypoints: List<LatLng>, destination: LatLng) {
        viewModelScope.launch {
            try {
                _isLoadingRoute.value = true
                _error.value = null

                Log.d(TAG, "🗺️ Buscando rota com ${waypoints.size} paradas...")

                val result = directionsService?.getDirectionsWithWaypoints(origin, waypoints, destination)

                if (result != null && result.routes.isNotEmpty()) {
                    val route = result.routes[0]

                    // Combinar todas as legs (segmentos) da rota
                    var totalDistance = 0
                    var totalDuration = 0
                    val allSteps = mutableListOf<RouteStep>()
                    val allPoints = mutableListOf<LatLng>()

                    route.legs.forEach { leg ->
                        totalDistance += leg.distance.inMeters.toInt()
                        totalDuration += leg.duration.inSeconds.toInt()

                        leg.steps.forEach { step ->
                            allSteps.add(RouteStep(
                                instruction = step.htmlInstructions
                                    .replace("<b>", "")
                                    .replace("</b>", "")
                                    .replace("<div[^>]*>", "")
                                    .replace("</div>", ""),
                                distance = step.distance.humanReadable,
                                duration = step.duration.humanReadable,
                                startLocation = LatLng(step.startLocation.lat, step.startLocation.lng),
                                endLocation = LatLng(step.endLocation.lat, step.endLocation.lng)
                            ))
                        }
                    }

                    // Decodificar polyline
                    val polyline = route.overviewPolyline.encodedPath
                    val points = directionsService?.decodePolyline(polyline) ?: emptyList()

                    val routeInfo = RouteInfo(
                        polylinePoints = points,
                        distanceText = formatDistance(totalDistance),
                        durationText = formatDuration(totalDuration),
                        distanceMeters = totalDistance,
                        durationSeconds = totalDuration,
                        steps = allSteps
                    )

                    _routeInfo.value = routeInfo
                    Log.d(TAG, "✅ Rota com paradas carregada: ${routeInfo.distanceText}, ${routeInfo.durationText}")

                } else {
                    _error.value = "Não foi possível encontrar uma rota"
                    Log.e(TAG, "❌ Nenhuma rota encontrada")
                }

            } catch (e: Exception) {
                _error.value = "Erro ao buscar rota: ${e.message}"
                Log.e(TAG, "❌ Erro ao buscar rota com paradas", e)
            } finally {
                _isLoadingRoute.value = false
            }
        }
    }

    /**
     * Busca a rota entre origem e destino
     */
    fun fetchRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            try {
                _isLoadingRoute.value = true
                _error.value = null

                Log.d(TAG, "🗺️ Buscando rota...")

                val result = directionsService?.getDirections(origin, destination)

                if (result != null && result.routes.isNotEmpty()) {
                    val route = result.routes[0]
                    val leg = route.legs[0]

                    // Decodificar polyline
                    val polyline = route.overviewPolyline.encodedPath
                    val points = directionsService?.decodePolyline(polyline) ?: emptyList()

                    // Extrair steps
                    val steps = leg.steps.map { step ->
                        RouteStep(
                            instruction = step.htmlInstructions
                                .replace("<b>", "")
                                .replace("</b>", "")
                                .replace("<div[^>]*>", "")
                                .replace("</div>", ""),
                            distance = step.distance.humanReadable,
                            duration = step.duration.humanReadable,
                            startLocation = LatLng(
                                step.startLocation.lat,
                                step.startLocation.lng
                            ),
                            endLocation = LatLng(
                                step.endLocation.lat,
                                step.endLocation.lng
                            )
                        )
                    }

                    val routeInfo = RouteInfo(
                        polylinePoints = points,
                        distanceText = leg.distance.humanReadable,
                        durationText = leg.duration.humanReadable,
                        distanceMeters = leg.distance.inMeters.toInt(),
                        durationSeconds = leg.duration.inSeconds.toInt(),
                        steps = steps
                    )

                    _routeInfo.value = routeInfo
                    Log.d(TAG, "✅ Rota carregada: ${routeInfo.distanceText}, ${routeInfo.durationText}")

                } else {
                    _error.value = "Não foi possível encontrar uma rota"
                    Log.e(TAG, "❌ Nenhuma rota encontrada")
                }

            } catch (e: Exception) {
                _error.value = "Erro ao buscar rota: ${e.message}"
                Log.e(TAG, "❌ Erro ao buscar rota", e)
            } finally {
                _isLoadingRoute.value = false
            }
        }
    }

    /**
     * Obtém localização atual
     */
    suspend fun getCurrentLocation(context: Context): LatLng? {
        initServices(context, "")
        val location = locationService?.getCurrentLocation()
        return location?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            _currentLocation.value = latLng
            latLng
        }
    }

    /**
     * Limpa o erro
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpa a rota
     */
    fun clearRoute() {
        _routeInfo.value = null
    }

    private fun formatDistance(meters: Int): String {
        return if (meters >= 1000) {
            val km = meters / 1000.0
            String.format(java.util.Locale.getDefault(), "%.1f km", km)
        } else {
            "$meters m"
        }
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes > 0) {
                "${hours}h ${remainingMinutes}min"
            } else {
                "${hours}h"
            }
        } else {
            "${minutes} min"
        }
    }
}

