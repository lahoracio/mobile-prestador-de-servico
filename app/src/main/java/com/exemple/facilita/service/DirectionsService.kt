package com.exemple.facilita.service

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Serviço para buscar rotas usando Google Directions API
 */
class DirectionsService(private val apiKey: String) {

    companion object {
        private const val TAG = "DirectionsService"
    }

    private val geoApiContext = GeoApiContext.Builder()
        .apiKey(apiKey)
        .build()

    /**
     * Busca a rota entre origem e destino
     */
    suspend fun getDirections(
        origin: LatLng,
        destination: LatLng
    ): DirectionsResult? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗺️ Buscando rota de ${origin.latitude},${origin.longitude} para ${destination.latitude},${destination.longitude}")

            val result = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .language("pt-BR")
                .await()

            Log.d(TAG, "✅ Rota encontrada com ${result.routes.size} opções")
            result

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar rota", e)
            null
        }
    }

    /**
     * Busca rota com paradas intermediárias (waypoints)
     */
    suspend fun getDirectionsWithWaypoints(
        origin: LatLng,
        waypoints: List<LatLng>,
        destination: LatLng
    ): DirectionsResult? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗺️ Buscando rota com paradas")
            Log.d(TAG, "   Origem: ${origin.latitude}, ${origin.longitude}")
            waypoints.forEachIndexed { index, waypoint ->
                Log.d(TAG, "   Parada ${index + 1}: ${waypoint.latitude}, ${waypoint.longitude}")
            }
            Log.d(TAG, "   Destino: ${destination.latitude}, ${destination.longitude}")

            val waypointsArray = waypoints.map {
                com.google.maps.model.LatLng(it.latitude, it.longitude)
            }.toTypedArray()

            val result = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .waypoints(*waypointsArray)
                .optimizeWaypoints(false) // Manter ordem das paradas
                .language("pt-BR")
                .await()

            Log.d(TAG, "✅ Rota com ${waypoints.size} paradas encontrada")
            result

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao buscar rota com paradas", e)
            null
        }
    }

    /**
     * Decodifica polyline para lista de LatLng
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}

/**
 * Modelo de rota simplificado
 */
data class RouteInfo(
    val polylinePoints: List<LatLng>,
    val distanceText: String,
    val durationText: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val steps: List<RouteStep>
)

/**
 * Passo da rota
 */
data class RouteStep(
    val instruction: String,
    val distance: String,
    val duration: String,
    val startLocation: LatLng,
    val endLocation: LatLng
)

