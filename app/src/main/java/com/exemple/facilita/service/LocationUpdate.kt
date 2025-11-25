package com.exemple.facilita.service

/**
 * Classe de dados para atualizações de localização em tempo real
 */
data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val userId: Int,
    val servicoId: Int,
    val prestadorName: String,
    val timestamp: String
)

