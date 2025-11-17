package com.exemple.facilita.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe
import com.exemple.facilita.viewmodel.MapaRotaViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapaRota(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe,
    mapaViewModel: MapaRotaViewModel = viewModel()
) {
    val context = LocalContext.current

    // Cores do app
    val primaryGreen = Color(0xFF019D31)

    // Estados
    val routeInfo by mapaViewModel.routeInfo.collectAsState()
    val currentLocation by mapaViewModel.currentLocation.collectAsState()
    val isLoadingRoute by mapaViewModel.isLoadingRoute.collectAsState()

    // Estado da UI
    var showSteps by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }

    // Localização do destino
    val destination = servicoDetalhe.localizacao?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(-23.5505, -46.6333)

    // Câmera do mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destination, 14f)
    }

    // Permissões
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        hasPermission = granted
    }

    // Solicitar permissões ao iniciar
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Buscar localização e rota quando tiver permissão
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val origin = mapaViewModel.getCurrentLocation(context)
            if (origin != null) {
                // Buscar rota
                mapaViewModel.initServices(
                    context,
                    context.getString(com.exemple.facilita.R.string.google_maps_key)
                )
                mapaViewModel.fetchRoute(origin, destination)
            }
        }
    }

    // Ajustar câmera quando rota carregar
    LaunchedEffect(routeInfo) {
        routeInfo?.let { route ->
            val bounds = LatLngBounds.builder().apply {
                route.polylinePoints.forEach { include(it) }
            }.build()

            delay(300)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = currentLocation != null,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            // Marcador de origem (localização atual)
            currentLocation?.let { origin ->
                Marker(
                    state = MarkerState(position = origin),
                    title = "Você está aqui",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Marcador de destino
            Marker(
                state = MarkerState(position = destination),
                title = servicoDetalhe.contratante.usuario.nome,
                snippet = servicoDetalhe.localizacao?.endereco ?: "",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )

            // Desenhar rota
            routeInfo?.let { route ->
                Polyline(
                    points = route.polylinePoints,
                    color = primaryGreen,
                    width = 12f,
                    pattern = null
                )
            }
        }

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            HeaderMapaRota(
                onBack = { navController.popBackStack() },
                isNavigating = isNavigating
            )

            // Card de informações da rota
            AnimatedVisibility(
                visible = routeInfo != null && !showSteps,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                routeInfo?.let { route ->
                    RouteInfoCard(
                        route = route,
                        servicoDetalhe = servicoDetalhe,
                        primaryGreen = primaryGreen,
                        onShowSteps = { showSteps = true }
                    )
                }
            }
        }

        // Loading
        if (isLoadingRoute) {
            LoadingOverlay()
        }

        // Botão de iniciar navegação
        AnimatedVisibility(
            visible = routeInfo != null && !isNavigating && !showSteps,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            StartNavigationButton(
                onClick = {
                    isNavigating = true
                    // Aqui você pode integrar com navegação real
                },
                primaryGreen = primaryGreen
            )
        }

        // Bottom sheet de passos
        AnimatedVisibility(
            visible = showSteps && routeInfo != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            routeInfo?.let { route ->
                StepsBottomSheet(
                    steps = route.steps,
                    onClose = { showSteps = false },
                    primaryGreen = primaryGreen
                )
            }
        }

        // Botão de centralizar
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(location, 16f)
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .padding(bottom = if (routeInfo != null) 100.dp else 0.dp),
            containerColor = Color.White
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "Minha localização",
                tint = primaryGreen
            )
        }
    }
}

@Composable
fun HeaderMapaRota(
    onBack: () -> Unit,
    isNavigating: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), CircleShape)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color(0xFF212121)
            )
        }

        Text(
            text = if (isNavigating) "Navegando..." else "Rota",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        IconButton(
            onClick = { /* Menu */ },
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), CircleShape)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Mais",
                tint = Color(0xFF212121)
            )
        }
    }
}

@Composable
fun RouteInfoCard(
    route: com.exemple.facilita.service.RouteInfo,
    servicoDetalhe: ServicoDetalhe,
    primaryGreen: Color,
    onShowSteps: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = servicoDetalhe.contratante.usuario.nome,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = servicoDetalhe.categoria.nome,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Text(
                    text = "R$ ${servicoDetalhe.valor}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RouteStatItem(
                    icon = Icons.Default.LocationOn,
                    label = "Distância",
                    value = route.distanceText,
                    color = primaryGreen
                )

                RouteStatItem(
                    icon = Icons.Default.Schedule,
                    label = "Tempo",
                    value = route.durationText,
                    color = primaryGreen
                )

                IconButton(
                    onClick = onShowSteps,
                    modifier = Modifier
                        .size(48.dp)
                        .background(primaryGreen.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Ver passos",
                        tint = primaryGreen
                    )
                }
            }
        }
    }
}

@Composable
fun RouteStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
    }
}

@Composable
fun StartNavigationButton(
    onClick: () -> Unit,
    primaryGreen: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(8.dp, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryGreen
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Navigation,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Iniciar Navegação",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun StepsBottomSheet(
    steps: List<com.exemple.facilita.service.RouteStep>,
    onClose: () -> Unit,
    primaryGreen: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instruções de Rota",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color(0xFF757575)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(steps) { step ->
                    StepItem(step = step, primaryGreen = primaryGreen)
                }
            }
        }
    }
}

@Composable
fun StepItem(
    step: com.exemple.facilita.service.RouteStep,
    primaryGreen: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.TurnRight,
            contentDescription = null,
            tint = primaryGreen,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.instruction,
                fontSize = 14.sp,
                color = Color(0xFF212121),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${step.distance} • ${step.duration}",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF019D31)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Calculando melhor rota...",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

