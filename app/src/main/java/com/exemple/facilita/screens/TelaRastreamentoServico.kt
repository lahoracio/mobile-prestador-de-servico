package com.exemple.facilita.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe
import com.exemple.facilita.service.LocationUpdate
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.RastreamentoViewModel
import com.exemple.facilita.viewmodel.ChatViewModel
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRastreamentoServico(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe,
    rastreamentoViewModel: RastreamentoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Cores do app (modo claro)
    val primaryGreen = Color(0xFF019D31)

    // Estados
    val myLocation by rastreamentoViewModel.myLocation.collectAsStateWithLifecycle()
    val otherUserLocation by rastreamentoViewModel.otherUserLocation.collectAsStateWithLifecycle()
    val isConnected by rastreamentoViewModel.isConnected.collectAsStateWithLifecycle()
    val connectionStatus by rastreamentoViewModel.connectionStatus.collectAsStateWithLifecycle()
    val isTracking by rastreamentoViewModel.isTracking.collectAsStateWithLifecycle()
    val lastUpdateState: LocationUpdate? by rastreamentoViewModel.lastUpdate.collectAsStateWithLifecycle()

    // Posição inicial do mapa (localização do serviço ou São Paulo)
    val initialPosition = servicoDetalhe.localizacao?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(-23.5505, -46.6333)

    // Estado da câmera do mapa
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }

    // Permissão de localização
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            // Iniciar rastreamento
            startTracking(context, servicoDetalhe, rastreamentoViewModel)
        }
    }

    // Verificar e solicitar permissões
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Animação de pulso para conexão
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Atualizar câmera quando localização mudar
    LaunchedEffect(myLocation) {
        myLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 16f),
                durationMs = 1000
            )
        }
    }

    // Cleanup ao sair
    DisposableEffect(Unit) {
        onDispose {
            rastreamentoViewModel.stopTracking()
        }
    }

    // Chat (campo de digitação) - inicializa e gerencia conexão de chat para este serviço
    val chatViewModel: ChatViewModel = viewModel()
    var chatMessageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(servicoDetalhe.id) {
        // Inicializa o chat com dados do prestador (se disponíveis)
        val prestadorUserId = servicoDetalhe.prestador?.id_usuario ?: 0
        val prestadorUserName = servicoDetalhe.prestador?.usuario?.nome ?: "Prestador"
        try {
            chatViewModel.initializeChat(
                servicoId = servicoDetalhe.id,
                userId = prestadorUserId,
                userName = prestadorUserName,
                userType = "prestador"
            )
        } catch (e: Exception) {
            // Fail silently; ChatViewModel logará
        }
    }

    DisposableEffect(servicoDetalhe.id) {
        onDispose {
            try {
                chatViewModel.leaveChat(servicoDetalhe.id)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rastreamento em Tempo Real",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) primaryGreen else Color.Red)
                                    .alpha(if (isConnected) pulseAlpha else 1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = connectionStatus,
                                fontSize = 12.sp,
                                color = if (isConnected) primaryGreen else Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF212121),
                    navigationIconContentColor = Color(0xFF212121)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapa do Google Maps
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false, // Vamos usar marcador customizado
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Marcador da minha localização
                myLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Você",
                        snippet = "Sua localização atual",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }

                // Marcador da outra pessoa
                otherUserLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = lastUpdateState?.prestadorName ?: "Outro usuário",
                        snippet = "Localização em tempo real",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
                }

                // Marcador do destino (localização do serviço)
                servicoDetalhe.localizacao?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "Destino",
                        snippet = loc.endereco,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED
                        )
                    )
                }
            }

            // Card flutuante com informações
            AnimatedVisibility(
                visible = isTracking,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                InfoCard(
                    servicoDetalhe = servicoDetalhe,
                    myLocation = myLocation,
                    otherUserLocation = otherUserLocation,
                    lastUpdate = lastUpdateState,
                    rastreamentoViewModel = rastreamentoViewModel,
                    primaryGreen = primaryGreen
                )
            }

            // Botão de centralizar
            myLocation?.let { location ->
                FloatingActionButton(
                    onClick = {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(location, 16f)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 180.dp),
                    containerColor = primaryGreen
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Centralizar",
                        tint = Color.White
                    )
                }
            }

            // Campo de digitação do chat sobre o mapa (fixo na parte inferior)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding(),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { newText ->
                                chatMessageText = newText
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Digite uma mensagem...") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = primaryGreen
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (chatMessageText.isNotBlank()) {
                                    try {
                                        chatViewModel.sendMessage(
                                            servicoId = servicoDetalhe.id,
                                            mensagem = chatMessageText.trim(),
                                            targetUserId = servicoDetalhe.id_contratante
                                        )
                                        chatMessageText = ""
                                        keyboardController?.hide()
                                    } catch (e: Exception) {
                                        Log.e("TelaRastreamentoServico", "Erro ao enviar mensagem: ${e.message}")
                                    }
                                }
                            }),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    try {
                                        chatViewModel.sendMessage(
                                            servicoId = servicoDetalhe.id,
                                            mensagem = chatMessageText.trim(),
                                            targetUserId = servicoDetalhe.id_contratante
                                        )
                                        chatMessageText = ""
                                        keyboardController?.hide()
                                    } catch (e: Exception) {
                                        Log.e("TelaRastreamentoServico", "Erro ao enviar mensagem: ${e.message}")
                                    }
                                }
                            },
                            containerColor = primaryGreen,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    servicoDetalhe: ServicoDetalhe,
    myLocation: LatLng?,
    otherUserLocation: LatLng?,
    lastUpdate: LocationUpdate?,
    rastreamentoViewModel: RastreamentoViewModel,
    primaryGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Indicador de drag
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E0E0))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Informações do serviço
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
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

            // Distância
            if (myLocation != null && otherUserLocation != null) {
                val distance = rastreamentoViewModel.calculateDistance(myLocation, otherUserLocation)
                val distanceText = rastreamentoViewModel.formatDistance(distance)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NearMe,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Distância",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Text(
                        text = distanceText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                }
            }

            // Última atualização
            lastUpdate?.let { update: LocationUpdate ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Última atualização",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Text(
                        text = try {
                            update.timestamp.substringAfter("T").substring(0, 8)
                        } catch (e: Exception) {
                            "N/A"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

/**
 * Função auxiliar para iniciar rastreamento
 */
private fun startTracking(
    context: android.content.Context,
    servicoDetalhe: ServicoDetalhe,
    rastreamentoViewModel: RastreamentoViewModel
) {
    // Obter dados do prestador
    val userId = TokenManager.obterUsuarioId(context) ?: 0
    val userName = TokenManager.obterNomeUsuario(context) ?: "Prestador"

    rastreamentoViewModel.startTracking(
        context = context,
        servicoId = servicoDetalhe.id,
        userId = userId,
        userType = "prestador",
        userName = userName
    )
}
