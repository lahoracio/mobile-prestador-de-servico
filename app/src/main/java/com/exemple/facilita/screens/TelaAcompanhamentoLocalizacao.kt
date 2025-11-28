package com.exemple.facilita.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.R
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.MapaRotaViewModel
import com.exemple.facilita.websocket.LocationSocketManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAcompanhamentoLocalizacao(
    navController: NavController,
    servicoId: Int,
    contratanteNome: String,
    servicoDetalhe: com.exemple.facilita.model.ServicoDetalhe? = null,
    mapaViewModel: MapaRotaViewModel = viewModel()
) {
    val context = LocalContext.current

    // Cores
    val primaryGreen = Color(0xFF00B14F)
    val bgLight = Color(0xFFFAFAFA)

    // Estados
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var myLocation by remember { mutableStateOf<LatLng?>(null) }
    var destino by remember { mutableStateOf<LatLng?>(null) }
    var paradas by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var paradasInfo by remember { mutableStateOf<List<com.exemple.facilita.model.ParadaDetalhe>>(emptyList()) }
    var isConnected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }
    var currentStepIndex by remember { mutableStateOf(0) }

    // Extrair origem, paradas e destino do serviço
    LaunchedEffect(servicoDetalhe) {
        servicoDetalhe?.paradas?.let { listaParadas ->
            android.util.Log.d("TelaAcompanhamento", "")
            android.util.Log.d("TelaAcompanhamento", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            android.util.Log.d("TelaAcompanhamento", "📋 EXTRAINDO PARADAS DO SERVIÇO")
            android.util.Log.d("TelaAcompanhamento", "   Total de pontos: ${listaParadas.size}")

            // Separar por tipo
            val origem = listaParadas.find { it.tipo == "ORIGEM" }
            val paradasOrdenadas = listaParadas
                .filter { it.tipo == "PARADA" }
                .sortedBy { it.ordem }
            val destinoFinal = listaParadas.find { it.tipo == "DESTINO" }

            // Log detalhado
            origem?.let {
                android.util.Log.d("TelaAcompanhamento", "   🟢 ORIGEM: ${it.descricao} (${it.lat}, ${it.lng})")
            }

            paradasOrdenadas.forEachIndexed { index, parada ->
                android.util.Log.d("TelaAcompanhamento", "   🟠 PARADA ${index + 1}: ${parada.descricao} (${parada.lat}, ${parada.lng})")
            }

            destinoFinal?.let {
                android.util.Log.d("TelaAcompanhamento", "   🔴 DESTINO: ${it.descricao} (${it.lat}, ${it.lng})")
            }
            android.util.Log.d("TelaAcompanhamento", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            android.util.Log.d("TelaAcompanhamento", "")

            // Salvar nos estados
            paradasInfo = paradasOrdenadas
            paradas = paradasOrdenadas.map { LatLng(it.lat, it.lng) }

            // Definir destino final
            destinoFinal?.let {
                destino = LatLng(it.lat, it.lng)
                android.util.Log.d("TelaAcompanhamento", "✅ Destino definido: ${it.descricao}")
            }

            // Se tiver origem E destino, buscar rota automaticamente quando tiver GPS
            if (origem != null && destinoFinal != null) {
                android.util.Log.d("TelaAcompanhamento", "✅ Serviço tem origem e destino, aguardando GPS para traçar rota...")
            }
        }
    }


    // Estados do ViewModel
    val routeInfo by mapaViewModel.routeInfo.collectAsState()
    val isLoadingRoute by mapaViewModel.isLoadingRoute.collectAsState()

    // Obter dados do usuário
    val userId = TokenManager.obterUsuarioId(context) ?: 0
    val userName = TokenManager.obterNomeUsuario(context) ?: "Prestador"

    // FusedLocationProviderClient para GPS
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Launcher para pedir permissão de localização
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!hasLocationPermission) {
            Toast.makeText(context, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    // Verificar permissão ao iniciar
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Gerenciador do WebSocket de localização
    val locationManager = remember { LocationSocketManager.getInstance() }

    // Conectar ao WebSocket
    LaunchedEffect(Unit) {
        android.util.Log.d("TelaAcompanhamento", "🚀 Conectando ao WebSocket de localização...")
        android.util.Log.d("TelaAcompanhamento", "   ServicoId: $servicoId")
        android.util.Log.d("TelaAcompanhamento", "   UserId: $userId")

        locationManager.connect(
            userId = userId,
            userType = "prestador",
            userName = userName,
            servicoId = servicoId,
            onLocationUpdated = { lat, lng, name, _ ->
                android.util.Log.d("TelaAcompanhamento", "")
                android.util.Log.d("TelaAcompanhamento", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                android.util.Log.d("TelaAcompanhamento", "📍 LOCALIZAÇÃO DO CONTRATANTE RECEBIDA")
                android.util.Log.d("TelaAcompanhamento", "   Nome: $name")
                android.util.Log.d("TelaAcompanhamento", "   Lat: $lat")
                android.util.Log.d("TelaAcompanhamento", "   Lng: $lng")
                android.util.Log.d("TelaAcompanhamento", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                android.util.Log.d("TelaAcompanhamento", "")

                val novoDestino = LatLng(lat, lng)
                destino = novoDestino

                // Buscar rota se tiver origem
                myLocation?.let { origem ->
                    android.util.Log.d("TelaAcompanhamento", "🗺️ Iniciando busca de rota...")
                    android.util.Log.d("TelaAcompanhamento", "   Origem (você): ${origem.latitude}, ${origem.longitude}")

                    if (paradas.isNotEmpty()) {
                        android.util.Log.d("TelaAcompanhamento", "   🛑 ${paradas.size} paradas intermediárias")
                        paradas.forEachIndexed { index, parada ->
                            android.util.Log.d("TelaAcompanhamento", "      Parada ${index + 1}: ${parada.latitude}, ${parada.longitude}")
                        }
                    }

                    android.util.Log.d("TelaAcompanhamento", "   Destino final: $lat, $lng")

                    // Inicializar serviços com API Key do Google Maps
                    val apiKey = context.getString(R.string.google_maps_key)
                    mapaViewModel.initServices(context, apiKey)

                    // Buscar rota (com ou sem waypoints)
                    if (paradas.isNotEmpty()) {
                        mapaViewModel.fetchRouteWithWaypoints(origem, paradas, novoDestino)
                    } else {
                        mapaViewModel.fetchRoute(origem, novoDestino)
                    }
                } ?: run {
                    android.util.Log.e("TelaAcompanhamento", "❌ Sua localização ainda não está disponível. Aguardando GPS...")

                    // Se não tem sua localização ainda, pelo menos mostrar o contratante no mapa
                    googleMap?.let { map ->
                        map.addMarker(
                            MarkerOptions()
                                .position(novoDestino)
                                .title(name)
                                .snippet("Contratante")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(novoDestino, 15f))
                    }
                }
            },
            onError = { error ->
                android.util.Log.e("TelaAcompanhamento", "❌ Erro: $error")
                errorMessage = error
            }
        )

        delay(1000)
        isConnected = locationManager.isConnected()
        android.util.Log.d("TelaAcompanhamento", "✅ Status de conexão: $isConnected")
    }

    // Callback de localização GPS
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    val oldLocation = myLocation
                    myLocation = newLatLng

                    android.util.Log.d("TelaAcompanhamento", "📍 Minha localização atualizada: ${location.latitude}, ${location.longitude}")

                    // Verificar conexão atual
                    val connected = locationManager.isConnected()
                    if (connected != isConnected) {
                        isConnected = connected
                        android.util.Log.d("TelaAcompanhamento", "🔄 Status de conexão atualizado: $isConnected")
                    }

                    // Enviar localização via WebSocket
                    if (isConnected) {
                        android.util.Log.d("TelaAcompanhamento", "📤 Enviando localização via WebSocket...")
                        locationManager.updateLocation(
                            servicoId = servicoId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            userId = userId
                        )
                        android.util.Log.d("TelaAcompanhamento", "✅ Localização enviada!")
                    } else {
                        android.util.Log.e("TelaAcompanhamento", "❌ WebSocket não conectado, não enviou localização")
                    }

                    // Se é a primeira vez que recebe localização, centralizar câmera
                    if (oldLocation == null && routeInfo == null) {
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
                        android.util.Log.d("TelaAcompanhamento", "📷 Câmera centralizada na primeira localização")
                    }

                    // Se tem destino mas ainda não tem rota, tentar buscar
                    if (destino != null && routeInfo == null) {
                        android.util.Log.d("TelaAcompanhamento", "🗺️ Tentar buscar rota agora que temos origem...")
                        val apiKey = context.getString(R.string.google_maps_key)
                        mapaViewModel.initServices(context, apiKey)

                        // Buscar rota com ou sem waypoints
                        if (paradas.isNotEmpty()) {
                            android.util.Log.d("TelaAcompanhamento", "   Com ${paradas.size} paradas intermediárias")
                            mapaViewModel.fetchRouteWithWaypoints(newLatLng, paradas, destino!!)
                        } else {
                            mapaViewModel.fetchRoute(newLatLng, destino!!)
                        }
                    }
                }
            }
        }
    }

    // Iniciar rastreamento de localização
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000 // Atualizar a cada 5 segundos
            ).apply {
                setMinUpdateIntervalMillis(3000) // Mínimo 3 segundos
                setWaitForAccurateLocation(true)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )

            android.util.Log.d("TelaAcompanhamento", "✅ Rastreamento GPS iniciado")
        } catch (e: SecurityException) {
            android.util.Log.e("TelaAcompanhamento", "❌ Erro de permissão: ${e.message}")
            errorMessage = "Erro ao acessar localização"
        }
    }

    // Desenhar marcadores quando houver localização mas não rota
    LaunchedEffect(myLocation, destino, routeInfo) {
        // Se não tem rota mas tem localizações, mostrar marcadores simples
        if (routeInfo == null) {
            googleMap?.let { map ->
                map.clear()

                android.util.Log.d("TelaAcompanhamento", "📍 Mostrando marcadores (sem rota)")

                // Marcador do prestador (verde)
                myLocation?.let { origem ->
                    map.addMarker(
                        MarkerOptions()
                            .position(origem)
                            .title("Você")
                            .snippet("Prestador")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    android.util.Log.d("TelaAcompanhamento", "  🟢 Marcador verde (você) adicionado")
                }

                // Marcador do contratante (vermelho)
                destino?.let { fim ->
                    map.addMarker(
                        MarkerOptions()
                            .position(fim)
                            .title(contratanteNome)
                            .snippet("Contratante")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                    android.util.Log.d("TelaAcompanhamento", "  🔴 Marcador vermelho (contratante) adicionado")
                }

                // Ajustar câmera para mostrar ambos
                if (myLocation != null && destino != null) {
                    val boundsBuilder = LatLngBounds.builder()
                    boundsBuilder.include(myLocation!!)
                    boundsBuilder.include(destino!!)
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                    android.util.Log.d("TelaAcompanhamento", "  📷 Câmera ajustada para mostrar ambos")
                }
            }
        }
    }

    // Desenhar rota no mapa quando routeInfo mudar
    LaunchedEffect(routeInfo) {
        routeInfo?.let { route ->
            googleMap?.let { map ->
                android.util.Log.d("TelaAcompanhamento", "🎨 Desenhando rota no mapa...")

                // Limpar mapa
                map.clear()

                // Desenhar polyline da rota (linha azul)
                if (route.polylinePoints.isNotEmpty()) {
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(route.polylinePoints)
                            .width(10f)
                            .color(android.graphics.Color.parseColor("#2196F3"))
                            .geodesic(true)
                    )
                }

                // Adicionar marcador de origem (VERDE - Você)
                myLocation?.let { origem ->
                    map.addMarker(
                        MarkerOptions()
                            .position(origem)
                            .title("Você (Origem)")
                            .snippet("Prestador")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                }

                // Adicionar marcadores numerados para as paradas intermediárias
                paradasInfo.forEachIndexed { index, parada ->
                    val position = LatLng(parada.lat, parada.lng)
                    map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("Parada ${index + 1}")
                            .snippet(parada.descricao)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    )
                    android.util.Log.d("TelaAcompanhamento", "  🟠 Parada ${index + 1}: ${parada.descricao}")
                }

                // Adicionar marcador de destino (VERMELHO - Destino Final)
                destino?.let { fim ->
                    map.addMarker(
                        MarkerOptions()
                            .position(fim)
                            .title("Destino Final")
                            .snippet(contratanteNome)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )
                }

                // Ajustar câmera para mostrar a rota completa
                if (route.polylinePoints.size >= 2) {
                    val boundsBuilder = LatLngBounds.builder()
                    route.polylinePoints.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
                }

                android.util.Log.d("TelaAcompanhamento", "✅ Rota desenhada com ${paradasInfo.size} paradas: ${route.distanceText}, ${route.durationText}")
            }
        }
    }

    // Limpar ao sair
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("TelaAcompanhamento", "📴 Saindo da tela de localização")
            fusedLocationClient.removeLocationUpdates(locationCallback)
            // NÃO desconectar WebSocket - manter ativo para outras telas
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Localização em Tempo Real",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = contratanteNome,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = bgLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Google Maps
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        onCreate(null)
                        getMapAsync { map ->
                            googleMap = map

                            // Configurações básicas
                            map.uiSettings.apply {
                                isZoomControlsEnabled = true
                                isMyLocationButtonEnabled = false
                                isCompassEnabled = true
                                isRotateGesturesEnabled = true
                                isScrollGesturesEnabled = true
                                isTiltGesturesEnabled = true
                                isZoomGesturesEnabled = true
                            }

                            // Habilitar "My Location" se tiver permissão
                            try {
                                if (hasLocationPermission) {
                                    map.isMyLocationEnabled = true
                                }
                            } catch (e: SecurityException) {
                                android.util.Log.e("TelaAcompanhamento", "Erro: ${e.message}")
                            }

                            // Posição inicial (São Paulo)
                            val initialPosition = LatLng(-23.55052, -46.633308)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 12f))

                            android.util.Log.d("TelaAcompanhamento", "✅ Mapa inicializado")
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    // Atualizar lifecycle
                    mapView.onResume()
                }
            )

            // Botões de ação (Chat e Localização)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão de Chat
                servicoDetalhe?.let { servico ->
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                "chat_ao_vivo/${servico.id}/${servico.id_contratante}/${contratanteNome}/${servico.id_prestador}/${servico.prestador?.usuario?.nome ?: "Prestador"}"
                            )
                        },
                        containerColor = Color(0xFF0097A7)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.White
                        )
                    }
                }

                // Botão para centralizar na sua localização
                FloatingActionButton(
                    onClick = {
                        myLocation?.let { location ->
                            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        } ?: run {
                            Toast.makeText(context, "Localização não disponível", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = primaryGreen
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Minha localização",
                        tint = Color.White
                    )
                }
            }

            // Indicador de status
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) primaryGreen else Color.Gray
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                    Text(
                        text = if (isConnected) "Conectado" else "Conectando...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Card de informações da rota (parte inferior)
            routeInfo?.let { route ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Título
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rota até ${contratanteNome}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Botão de instruções
                            IconButton(
                                onClick = { showInstructions = !showInstructions }
                            ) {
                                Icon(
                                    if (showInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Instruções",
                                    tint = primaryGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Informações principais
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Distância
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = route.distanceText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Distância",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Tempo
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = route.durationText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tempo estimado",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Instruções de navegação (expansível)
                        if (showInstructions && route.steps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Instruções de navegação:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Lista de instruções (limitado a altura)
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(route.steps) { index, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Número do passo
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(
                                                    if (index == currentStepIndex) primaryGreen else Color.LightGray,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Instrução
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = android.text.Html.fromHtml(
                                                    step.instruction,
                                                    android.text.Html.FROM_HTML_MODE_LEGACY
                                                ).toString(),
                                                fontSize = 14.sp,
                                                lineHeight = 18.sp
                                            )
                                            Text(
                                                text = "${step.distance} • ${step.duration}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading da rota
            if (isLoadingRoute) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = primaryGreen,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Calculando rota...",
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Mensagem de erro
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

