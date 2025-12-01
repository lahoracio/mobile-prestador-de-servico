package com.exemple.facilita.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.exemple.facilita.viewmodel.ServicoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TelaRotaNavegacao(
    navController: NavController,
    servicoId: Int,
    servicoViewModel: ServicoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val servicoState by servicoViewModel.servicoState.collectAsState()
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isNavigating by remember { mutableStateOf(false) }
    var distanciaRestante by remember { mutableStateOf("Calculando...") }
    var tempoRestante by remember { mutableStateOf("Calculando...") }

    val primaryGreen = Color(0xFF2E7D32)
    val accentBlue = Color(0xFF1976D2)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(servicoId) {
        servicoViewModel.carregarServico(servicoId, context)
    }

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(servicoState.servico, locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted && servicoState.servico != null) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                @SuppressLint("MissingPermission")
                val location = fusedLocationClient.lastLocation.await()
                currentLocation = location
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Erro ao obter localização: ${e.message}"
                isLoading = false
            }
        }
    }

    LaunchedEffect(isNavigating, currentLocation) {
        if (isNavigating && locationPermissions.allPermissionsGranted) {
            while (isNavigating) {
                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    @SuppressLint("MissingPermission")
                    val location = fusedLocationClient.lastLocation.await()
                    currentLocation = location

                    googleMap?.let { map ->
                        val newPosition = LatLng(location.latitude, location.longitude)

                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(newPosition)
                                    .zoom(18f)
                                    .tilt(60f)
                                    .bearing(location.bearing)
                                    .build()
                            )
                        )

                        servicoState.servico?.localizacao?.let { destino ->
                            val dest = LatLng(
                                destino.latitude,
                                destino.longitude
                            )
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                location.latitude,
                                location.longitude,
                                dest.latitude,
                                dest.longitude,
                                results
                            )

                            val distancia = results[0]
                            distanciaRestante = if (distancia < 1000) {
                                "${distancia.toInt()} m"
                            } else {
                                "%.1f km".format(distancia / 1000)
                            }

                            val velocidade = if (location.speed > 0) location.speed else 5f
                            val tempoSegundos = (distancia / velocidade).toInt()
                            tempoRestante = if (tempoSegundos < 60) {
                                "$tempoSegundos seg"
                            } else {
                                "${tempoSegundos / 60} min"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignorar erro
                }
                delay(2000)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        if (!locationPermissions.allPermissionsGranted) {
            PermissaoLocalizacaoScreen(
                onRequest = { locationPermissions.launchMultiplePermissionRequest() },
                onBack = { navController.popBackStack() }
            )
        } else if (isLoading) {
            CarregandoRotaScreen()
        } else if (errorMessage != null) {
            ErroRotaScreen(
                message = errorMessage ?: "",
                onRetry = {
                    isLoading = true
                    errorMessage = null
                    scope.launch { servicoViewModel.carregarServico(servicoId, context) }
                },
                onBack = { navController.popBackStack() }
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        onResume()
                        getMapAsync { map ->
                            googleMap = map
                            configurarMapa3D(
                                map = map,
                                currentLoc = currentLocation,
                                servico = servicoState.servico
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.fillMaxSize()) {
                HeaderNavegacao(
                    servico = servicoState.servico,
                    onBack = {
                        isNavigating = false
                        navController.popBackStack()
                    },
                    primaryGreen = primaryGreen
                )

                Spacer(modifier = Modifier.weight(1f))

                PainelInstrucoesNavegacao(
                    isNavigating = isNavigating,
                    distancia = distanciaRestante,
                    tempo = tempoRestante,
                    onStart = { isNavigating = true },
                    onStop = { isNavigating = false },
                    primaryGreen = primaryGreen,
                    accentBlue = accentBlue,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            BotoesFlutantes(
                modifier = Modifier.align(Alignment.CenterEnd),
                currentLocation = currentLocation,
                googleMap = googleMap,
                primaryGreen = primaryGreen,
                accentBlue = accentBlue
            )
        }
    }
}

@Composable
fun HeaderNavegacao(
    servico: com.exemple.facilita.model.ServicoDetalhe?,
    onBack: () -> Unit,
    primaryGreen: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(primaryGreen.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, "Voltar", tint = primaryGreen)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Navegação 3D",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    servico?.contratante?.usuario?.nome ?: "Destino",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    maxLines = 1
                )
            }

            Icon(
                Icons.Default.Navigation,
                contentDescription = null,
                tint = primaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun PainelInstrucoesNavegacao(
    isNavigating: Boolean,
    distancia: String,
    tempo: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    primaryGreen: Color,
    accentBlue: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryGreen, Color(0xFF1B5E20))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Straight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isNavigating) "Siga em frente" else "Pronto para navegar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        lineHeight = 24.sp
                    )

                    if (isNavigating) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Continue pela rota",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = textSecondary.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoChipNavegacao(
                    icon = Icons.Default.Straighten,
                    label = "Distância",
                    value = distancia,
                    color = primaryGreen
                )

                InfoChipNavegacao(
                    icon = Icons.Default.Schedule,
                    label = "Tempo",
                    value = tempo,
                    color = accentBlue
                )

                InfoChipNavegacao(
                    icon = Icons.Default.Speed,
                    label = "Chegada",
                    value = calcularHoraChegada(tempo),
                    color = Color(0xFFFF6F00)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (isNavigating) onStop() else onStart() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNavigating) Color(0xFFE53935) else primaryGreen
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(
                    if (isNavigating) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isNavigating) "Parar Navegação" else "Iniciar Navegação",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun InfoChipNavegacao(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF757575))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
    }
}

@Composable
fun BotoesFlutantes(
    modifier: Modifier,
    currentLocation: Location?,
    googleMap: GoogleMap?,
    primaryGreen: Color,
    accentBlue: Color
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingActionButton(
            onClick = {
                currentLocation?.let { loc ->
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(loc.latitude, loc.longitude))
                                .zoom(18f)
                                .tilt(60f)
                                .bearing(loc.bearing)
                                .build()
                        )
                    )
                }
            },
            containerColor = Color.White,
            contentColor = primaryGreen
        ) {
            Icon(Icons.Default.MyLocation, "Centralizar")
        }

        FloatingActionButton(
            onClick = { googleMap?.animateCamera(CameraUpdateFactory.zoomIn()) },
            containerColor = Color.White,
            contentColor = Color(0xFF212121)
        ) {
            Icon(Icons.Default.Add, "Zoom In")
        }

        FloatingActionButton(
            onClick = { googleMap?.animateCamera(CameraUpdateFactory.zoomOut()) },
            containerColor = Color.White,
            contentColor = Color(0xFF212121)
        ) {
            Icon(Icons.Default.Remove, "Zoom Out")
        }

        FloatingActionButton(
            onClick = {
                googleMap?.let { map ->
                    val currentTilt = map.cameraPosition.tilt
                    val newTilt = if (currentTilt > 30f) 0f else 60f
                    map.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder(map.cameraPosition)
                                .tilt(newTilt)
                                .build()
                        )
                    )
                }
            },
            containerColor = Color.White,
            contentColor = accentBlue
        ) {
            Icon(Icons.Default.Terrain, "Vista 3D")
        }
    }
}

@Composable
fun PermissaoLocalizacaoScreen(
    onRequest: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFF2E7D32).copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Permissão de Localização",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Para usar a navegação 3D, precisamos acessar sua localização em tempo real",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequest,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.LocationOn, "Permitir")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Permitir Localização", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) {
            Text("Voltar", color = Color(0xFF757575))
        }
    }
}

@Composable
fun CarregandoRotaScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color(0xFF2E7D32),
                strokeWidth = 6.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Carregando rota...", fontSize = 16.sp, color = Color(0xFF757575))
        }
    }
}

@Composable
fun ErroRotaScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFE53935).copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Erro ao carregar rota",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, fontSize = 14.sp, color = Color(0xFF757575), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Voltar")
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, "Tentar novamente")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tentar Novamente")
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun configurarMapa3D(
    map: GoogleMap,
    currentLoc: Location?,
    servico: com.exemple.facilita.model.ServicoDetalhe?
) {
    map.mapType = GoogleMap.MAP_TYPE_NORMAL
    map.isMyLocationEnabled = true
    map.uiSettings.apply {
        isMyLocationButtonEnabled = false
        isCompassEnabled = true
        isZoomControlsEnabled = false
        isTiltGesturesEnabled = true
        isRotateGesturesEnabled = true
    }

    currentLoc?.let { loc ->
        val currentPosition = LatLng(loc.latitude, loc.longitude)

        map.addMarker(
            MarkerOptions()
                .position(currentPosition)
                .title("Sua Localização")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        servico?.localizacao?.let { destino ->
            val destinationPosition = LatLng(
                destino.latitude,
                destino.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(destinationPosition)
                    .title(servico.contratante.usuario.nome)
                    .snippet(destino.cidade ?: "Destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )

            map.addPolyline(
                PolylineOptions()
                    .add(currentPosition, destinationPosition)
                    .color(android.graphics.Color.rgb(46, 125, 50))
                    .width(15f)
                    .geodesic(true)
            )

            val bounds = LatLngBounds.Builder()
                .include(currentPosition)
                .include(destinationPosition)
                .build()

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(currentPosition)
                        .zoom(16f)
                        .tilt(60f)
                        .bearing(loc.bearing)
                        .build()
                ),
                2000,
                null
            )
        }
    }
}

fun calcularHoraChegada(tempo: String): String {
    val minutos = tempo.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MINUTE, minutos)

    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)

    return String.format("%02d:%02d", hour, minute)
}

