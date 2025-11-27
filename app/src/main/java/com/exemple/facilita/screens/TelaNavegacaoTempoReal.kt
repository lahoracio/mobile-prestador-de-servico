package com.exemple.facilita.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.viewmodel.NavegacaoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TelaNavegacaoTempoReal(
    navController: NavController,
    origemLat: Double,
    origemLng: Double,
    destinoLat: Double,
    destinoLng: Double,
    paradasJson: String? = null, // JSON com array de paradas
    viewModel: NavegacaoViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Permissões de localização
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val origem = remember { LatLng(origemLat, origemLng) }
    val destino = remember { LatLng(destinoLat, destinoLng) }

    // Parsear paradas se existirem
    val paradas = remember {
        // TODO: Parsear JSON de paradas
        emptyList<LatLng>()
    }

    // Iniciar navegação quando tiver permissões
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.iniciarNavegacao(context, origem, destino, paradas)
        }
    }

    // Configurar câmera do mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origem, 16f)
    }

    // Atualizar câmera quando localização mudar
    LaunchedEffect(state.localizacaoAtual) {
        state.localizacaoAtual?.let { localizacao ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(localizacao, 18f),
                durationMs = 1000
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!locationPermissions.allPermissionsGranted) {
            // Tela de permissões
            PermissoesLocalizacaoScreen(
                onRequestPermissions = { locationPermissions.launchMultiplePermissionRequest() }
            )
        } else {
            // Mapa principal
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                )
            ) {
                // Polyline da rota
                if (state.rotaPolyline.isNotEmpty()) {
                    Polyline(
                        points = state.rotaPolyline,
                        color = Color(0xFF0066FF),
                        width = 10f,
                        pattern = listOf(Dot(), Gap(10f))
                    )
                }

                // Marcador de origem
                Marker(
                    state = MarkerState(position = origem),
                    title = "Origem",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                // Marcadores de paradas
                state.pontos.filter { it.descricao.contains("Parada") }.forEach { ponto ->
                    Marker(
                        state = MarkerState(position = ponto.posicao),
                        title = ponto.descricao,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                }

                // Marcador de destino
                Marker(
                    state = MarkerState(position = destino),
                    title = "Destino",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            // UI Overlay - Informações de navegação
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header com botão voltar e info
                NavegacaoHeader(
                    onVoltarClick = {
                        viewModel.pararNavegacao()
                        navController.popBackStack()
                    },
                    tempoRestante = state.tempoRestante,
                    distanciaRestante = state.distanciaRestante
                )

                Spacer(modifier = Modifier.weight(1f))

                // Card de direções (principal)
                state.direcaoAtual?.let { direcao ->
                    CardDirecaoAtual(
                        direcao = direcao,
                        velocidadeAtual = state.velocidadeAtual
                    )
                }

                // Barra inferior com controles
                BarraControlesNavegacao(
                    onRecalcularRota = { viewModel.recalcularRota() },
                    onPararNavegacao = {
                        viewModel.pararNavegacao()
                        navController.popBackStack()
                    }
                )
            }

            // Dialog de chegada ao destino
            if (state.chegouAoDestino) {
                DialogChegadaDestino(
                    onConfirmar = {
                        viewModel.pararNavegacao()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun NavegacaoHeader(
    onVoltarClick: () -> Unit,
    tempoRestante: Int,
    distanciaRestante: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = Color(0xFF1A1A2E).copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão voltar
            IconButton(
                onClick = onVoltarClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar navegação",
                    tint = Color(0xFF00E676)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informações de tempo e distância
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$tempoRestante min",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatarDistancia(distanciaRestante),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CardDirecaoAtual(
    direcao: com.exemple.facilita.viewmodel.DirecoesNavegacao,
    velocidadeAtual: Float
) {
    // Animação de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulso")
    val pulso by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulso"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Ícone da direção
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0066FF).copy(alpha = 0.2f),
                                Color(0xFF0066FF).copy(alpha = 0.05f)
                            )
                        )
                    )
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconeDirecao(direcao.icone),
                    contentDescription = null,
                    tint = Color(0xFF0066FF),
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = pulso
                            scaleY = pulso
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instrução
            Text(
                text = direcao.instrucao,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Distância até próxima ação
            Text(
                text = "em ${direcao.distancia}",
                fontSize = 18.sp,
                color = Color(0xFF666666),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Velocidade atual
            if (velocidadeAtual > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${velocidadeAtual.toInt()} km/h",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00E676)
                    )
                }
            }
        }
    }
}

@Composable
fun BarraControlesNavegacao(
    onRecalcularRota: () -> Unit,
    onPararNavegacao: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botão recalcular
            FloatingActionButton(
                onClick = onRecalcularRota,
                containerColor = Color(0xFF0066FF),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recalcular rota",
                    tint = Color.White
                )
            }

            // Botão parar navegação
            FloatingActionButton(
                onClick = onPararNavegacao,
                containerColor = Color(0xFFFF3D00),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Parar navegação",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PermissoesLocalizacaoScreen(
    onRequestPermissions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permissão de Localização Necessária",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Para usar a navegação em tempo real, precisamos acessar sua localização.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequestPermissions,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Permitir Acesso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }
        }
    }
}

@Composable
fun DialogChegadaDestino(
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirmar,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Você Chegou!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Você chegou ao seu destino. A navegação será encerrada.",
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                )
            ) {
                Text("OK", color = Color(0xFF1A1A2E))
            }
        }
    )
}

private fun getIconeDirecao(icone: String): ImageVector {
    return when (icone) {
        "left" -> Icons.Default.TurnLeft
        "right" -> Icons.Default.TurnRight
        "uturn" -> Icons.Default.UTurnLeft
        else -> Icons.Default.ArrowUpward
    }
}

private fun formatarDistancia(distanciaMetros: Float): String {
    return when {
        distanciaMetros < 1000 -> "${distanciaMetros.toInt()} m"
        else -> String.format("%.1f km", distanciaMetros / 1000f)
    }
}
