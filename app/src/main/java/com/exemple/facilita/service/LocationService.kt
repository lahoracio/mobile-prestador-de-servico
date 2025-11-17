package com.exemple.facilita.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Serviço de Localização para rastreamento GPS contínuo
 */
class LocationService(private val context: Context) {

    companion object {
        private const val TAG = "LocationService"
        private const val UPDATE_INTERVAL = 5000L // 5 segundos
        private const val FASTEST_INTERVAL = 3000L // 3 segundos
        private const val MIN_DISTANCE = 10f // 10 metros
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    /**
     * Verifica se as permissões de localização estão concedidas
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtém a localização atual uma única vez
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Permissão de localização não concedida")
            return null
        }

        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter localização atual", e)
            null
        }
    }

    /**
     * Inicia o rastreamento contínuo de localização
     * Retorna um Flow que emite atualizações de localização
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Permissão de localização não concedida")
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            setMinUpdateDistanceMeters(MIN_DISTANCE)
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d(TAG, "📍 Nova localização: ${location.latitude}, ${location.longitude}")
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            Log.d(TAG, "✅ Rastreamento de localização iniciado")
        } catch (e: SecurityException) {
            Log.e(TAG, "Erro de permissão ao iniciar rastreamento", e)
            close(e)
        }

        awaitClose {
            Log.d(TAG, "🛑 Parando rastreamento de localização")
            stopLocationUpdates()
        }
    }

    /**
     * Para o rastreamento de localização
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            Log.d(TAG, "✅ Rastreamento de localização parado")
        }
    }
}


