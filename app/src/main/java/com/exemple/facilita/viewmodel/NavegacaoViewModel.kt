package com.exemple.facilita.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PontoRota(
    val posicao: LatLng,
    val descricao: String,
    val distancia: Float = 0f,
    val tempoEstimado: Int = 0 // em minutos
)

data class DirecoesNavegacao(
    val instrucao: String,
    val distancia: String,
    val icone: String, // "left", "right", "straight", "uturn"
    val proximaRua: String = ""
)

data class NavegacaoState(
    val localizacaoAtual: LatLng? = null,
    val destino: LatLng? = null,
    val pontos: List<PontoRota> = emptyList(),
    val rotaPolyline: List<LatLng> = emptyList(),
    val distanciaTotal: Float = 0f, // em metros
    val distanciaRestante: Float = 0f,
    val tempoEstimado: Int = 0, // em minutos
    val tempoRestante: Int = 0,
    val velocidadeAtual: Float = 0f, // em km/h
    val direcaoAtual: DirecoesNavegacao? = null,
    val proximaDirecao: DirecoesNavegacao? = null,
    val isNavigating: Boolean = false,
    val chegouAoDestino: Boolean = false,
    val erro: String? = null
)

class NavegacaoViewModel : ViewModel() {
    private val TAG = "NavegacaoViewModel"

    private val _state = MutableStateFlow(NavegacaoState())
    val state: StateFlow<NavegacaoState> = _state.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var ultimaLocalizacao: Location? = null

    @SuppressLint("MissingPermission")
    fun iniciarNavegacao(context: Context, origem: LatLng, destino: LatLng, paradas: List<LatLng> = emptyList()) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗺️ Iniciando navegação")
                Log.d(TAG, "   Origem: ${origem.latitude}, ${origem.longitude}")
                Log.d(TAG, "   Destino: ${destino.latitude}, ${destino.longitude}")
                Log.d(TAG, "   Paradas: ${paradas.size}")

                // Configurar pontos da rota
                val pontos = mutableListOf<PontoRota>()
                pontos.add(PontoRota(origem, "Origem"))
                paradas.forEachIndexed { index, parada ->
                    pontos.add(PontoRota(parada, "Parada ${index + 1}"))
                }
                pontos.add(PontoRota(destino, "Destino"))

                // Calcular rota (simulada - conecta os pontos)
                val rotaPolyline = calcularRotaPolyline(pontos.map { it.posicao })

                // Calcular distância e tempo
                val distanciaTotal = calcularDistanciaTotal(rotaPolyline)
                val tempoEstimado = calcularTempoEstimado(distanciaTotal)

                _state.value = _state.value.copy(
                    localizacaoAtual = origem,
                    destino = destino,
                    pontos = pontos,
                    rotaPolyline = rotaPolyline,
                    distanciaTotal = distanciaTotal,
                    distanciaRestante = distanciaTotal,
                    tempoEstimado = tempoEstimado,
                    tempoRestante = tempoEstimado,
                    isNavigating = true,
                    direcaoAtual = gerarDirecaoInicial(origem, pontos.getOrNull(1)?.posicao ?: destino)
                )

                // Iniciar tracking de localização
                iniciarTrackingLocalizacao(context)

                Log.d(TAG, "✅ Navegação iniciada com sucesso")
                Log.d(TAG, "   Distância total: ${distanciaTotal.toInt()}m")
                Log.d(TAG, "   Tempo estimado: $tempoEstimado min")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao iniciar navegação: ${e.message}")
                _state.value = _state.value.copy(erro = e.message)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarTrackingLocalizacao(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // Atualização a cada 2 segundos
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMaxUpdateDelayMillis(5000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    atualizarLocalizacao(location)
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null
        )

        Log.d(TAG, "📍 Tracking de localização iniciado")
    }

    private fun atualizarLocalizacao(location: Location) {
        val novaLocalizacao = LatLng(location.latitude, location.longitude)
        val velocidade = location.speed * 3.6f // Converter m/s para km/h

        val state = _state.value

        // Calcular distância restante até o destino
        val distanciaRestante = state.destino?.let { destino ->
            calcularDistancia(novaLocalizacao, destino)
        } ?: 0f

        // Verificar se chegou ao destino (menos de 50 metros)
        val chegouAoDestino = distanciaRestante < 50f

        // Calcular tempo restante baseado na velocidade atual
        val tempoRestante = if (velocidade > 0) {
            ((distanciaRestante / 1000f) / velocidade * 60).toInt() // minutos
        } else {
            state.tempoRestante
        }

        // Gerar próxima direção
        val proximoPonto = encontrarProximoPonto(novaLocalizacao, state.pontos)
        val direcaoAtual = proximoPonto?.let { ponto ->
            gerarDirecao(novaLocalizacao, ponto.posicao, ultimaLocalizacao)
        }

        _state.value = state.copy(
            localizacaoAtual = novaLocalizacao,
            distanciaRestante = distanciaRestante,
            tempoRestante = tempoRestante,
            velocidadeAtual = velocidade,
            direcaoAtual = direcaoAtual,
            chegouAoDestino = chegouAoDestino
        )

        ultimaLocalizacao = location

        if (chegouAoDestino) {
            Log.d(TAG, "🎯 Chegou ao destino!")
            pararNavegacao()
        }
    }

    private fun encontrarProximoPonto(localizacaoAtual: LatLng, pontos: List<PontoRota>): PontoRota? {
        // Encontrar o próximo ponto que ainda não foi alcançado
        return pontos.find { ponto ->
            calcularDistancia(localizacaoAtual, ponto.posicao) > 30f // Mais de 30 metros
        }
    }

    private fun gerarDirecaoInicial(origem: LatLng, destino: LatLng): DirecoesNavegacao {
        val distancia = calcularDistancia(origem, destino)
        return DirecoesNavegacao(
            instrucao = "Siga em frente",
            distancia = formatarDistancia(distancia),
            icone = "straight"
        )
    }

    private fun gerarDirecao(
        localizacaoAtual: LatLng,
        proximoPonto: LatLng,
        localizacaoAnterior: Location?
    ): DirecoesNavegacao {
        val distancia = calcularDistancia(localizacaoAtual, proximoPonto)

        // Calcular ângulo de direção
        val angulo = calcularAngulo(localizacaoAtual, proximoPonto)

        // Determinar instrução baseada no ângulo
        val (instrucao, icone) = when {
            angulo < -45 -> "Vire à esquerda" to "left"
            angulo > 45 -> "Vire à direita" to "right"
            angulo < -135 || angulo > 135 -> "Faça o retorno" to "uturn"
            else -> "Siga em frente" to "straight"
        }

        return DirecoesNavegacao(
            instrucao = instrucao,
            distancia = formatarDistancia(distancia),
            icone = icone
        )
    }

    private fun calcularAngulo(origem: LatLng, destino: LatLng): Double {
        val dLon = Math.toRadians(destino.longitude - origem.longitude)
        val lat1 = Math.toRadians(origem.latitude)
        val lat2 = Math.toRadians(destino.latitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        return Math.toDegrees(atan2(y, x))
    }

    private fun calcularRotaPolyline(pontos: List<LatLng>): List<LatLng> {
        // Criar uma rota interpolada entre os pontos
        val polyline = mutableListOf<LatLng>()

        for (i in 0 until pontos.size - 1) {
            val inicio = pontos[i]
            val fim = pontos[i + 1]

            // Adicionar ponto inicial
            polyline.add(inicio)

            // Interpolar pontos entre início e fim para criar curva suave
            val numPontosInterpolados = 10
            for (j in 1 until numPontosInterpolados) {
                val fraction = j.toDouble() / numPontosInterpolados
                val lat = inicio.latitude + (fim.latitude - inicio.latitude) * fraction
                val lng = inicio.longitude + (fim.longitude - inicio.longitude) * fraction
                polyline.add(LatLng(lat, lng))
            }
        }

        // Adicionar último ponto
        polyline.add(pontos.last())

        return polyline
    }

    private fun calcularDistancia(origem: LatLng, destino: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            origem.latitude,
            origem.longitude,
            destino.latitude,
            destino.longitude,
            results
        )
        return results[0]
    }

    private fun calcularDistanciaTotal(polyline: List<LatLng>): Float {
        var distanciaTotal = 0f
        for (i in 0 until polyline.size - 1) {
            distanciaTotal += calcularDistancia(polyline[i], polyline[i + 1])
        }
        return distanciaTotal
    }

    private fun calcularTempoEstimado(distanciaMetros: Float): Int {
        // Assumir velocidade média de 30 km/h em cidade
        val velocidadeMedia = 30f // km/h
        val distanciaKm = distanciaMetros / 1000f
        return ((distanciaKm / velocidadeMedia) * 60).toInt() // minutos
    }

    private fun formatarDistancia(distanciaMetros: Float): String {
        return when {
            distanciaMetros < 1000 -> "${distanciaMetros.toInt()} m"
            else -> String.format("%.1f km", distanciaMetros / 1000f)
        }
    }

    fun pararNavegacao() {
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }

        _state.value = _state.value.copy(
            isNavigating = false
        )

        Log.d(TAG, "⏹️ Navegação parada")
    }

    fun recalcularRota() {
        val state = _state.value
        if (state.localizacaoAtual != null && state.destino != null) {
            // Recalcular rota a partir da posição atual
            val novosPontos = state.pontos.filter { ponto ->
                calcularDistancia(state.localizacaoAtual, ponto.posicao) > 30f
            }

            if (novosPontos.isNotEmpty()) {
                val rotaPolyline = calcularRotaPolyline(
                    listOf(state.localizacaoAtual) + novosPontos.map { it.posicao }
                )

                val distanciaTotal = calcularDistanciaTotal(rotaPolyline)
                val tempoEstimado = calcularTempoEstimado(distanciaTotal)

                _state.value = state.copy(
                    rotaPolyline = rotaPolyline,
                    distanciaRestante = distanciaTotal,
                    tempoRestante = tempoEstimado
                )

                Log.d(TAG, "🔄 Rota recalculada")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pararNavegacao()
    }
}

