package com.exemple.facilita.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.CallState
import com.exemple.facilita.model.CallType
import com.exemple.facilita.viewmodel.CallViewModel
import kotlinx.coroutines.delay
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.EglBase
import org.webrtc.PeerConnection

@Composable
fun TelaVideoCall(
    navController: NavController,
    servicoId: Int,
    userId: Int,
    userName: String,
    targetUserId: Int,
    targetUserName: String,
    callType: String,
    callViewModel: CallViewModel = viewModel()
) {
    val context = LocalContext.current
    val callState by callViewModel.callState.collectAsState()
    val localStream by callViewModel.localStream.collectAsState()
    val remoteStream by callViewModel.remoteStream.collectAsState()
    val isVideoEnabled by callViewModel.isVideoEnabled.collectAsState()
    val isAudioEnabled by callViewModel.isAudioEnabled.collectAsState()
    val connectionState by callViewModel.connectionState.collectAsState()

    val eglBase = remember { EglBase.create() }
    var callDuration by remember { mutableStateOf(0L) }
    var isInitialized by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Cores
    val primaryGreen = Color(0xFF2E7D32)
    val darkBackground = Color(0xFF1A1A1A)
    val overlayColor = Color(0xFF000000).copy(alpha = 0.7f)

    // Animação de pulso para "Chamando..."
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Permissões
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (cameraGranted && audioGranted) {
            if (!isInitialized) {
                callViewModel.initialize(context, userId, userName, servicoId)
                callViewModel.startCall(
                    targetUserId,
                    targetUserName,
                    if (callType == "video") CallType.VIDEO else CallType.AUDIO
                )
                isInitialized = true
            }
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    // Timer
    LaunchedEffect(callState) {
        if (callState is CallState.ActiveCall) {
            while (true) {
                delay(1000)
                callDuration++
            }
        }
    }

    // Mostrar toast se usuário offline (mas não fechar)
    LaunchedEffect(callState) {
        if (callState is CallState.Error) {
            val errorMsg = (callState as CallState.Error).message
            if (errorMsg.contains("offline", ignoreCase = true)) {
                snackbarHostState.showSnackbar(
                    message = "⚠️ $targetUserName está offline no momento",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Auto voltar em caso de OUTROS erros ou término
    LaunchedEffect(callState) {
        when (callState) {
            is CallState.Error -> {
                val errorMsg = (callState as CallState.Error).message
                // Se NÃO for erro de offline, volta após 2 segundos
                if (!errorMsg.contains("offline", ignoreCase = true)) {
                    delay(2000)
                    navController.popBackStack()
                }
            }
            is CallState.Ended -> {
                delay(2000)
                navController.popBackStack()
            }
            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            eglBase.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Estado: Aguardando conexão / Chamando
        if (callState is CallState.OutgoingCall || callState is CallState.Connecting ||
            (callState is CallState.ActiveCall && remoteStream == null)) {

            CallingScreen(
                targetUserName = targetUserName,
                callType = callType,
                pulseScale = pulseScale,
                localStream = localStream,
                eglBase = eglBase,
                onEndCall = {
                    callViewModel.endCall()
                    navController.popBackStack()
                }
            )
        }
        // Estado: Chamada Ativa (com vídeo remoto)
        else if (callState is CallState.ActiveCall && remoteStream != null) {

            ActiveCallScreen(
                eglBase = eglBase,
                localStream = localStream,
                remoteStream = remoteStream,
                callType = callType,
                isVideoEnabled = isVideoEnabled,
                isAudioEnabled = isAudioEnabled,
                callDuration = callDuration,
                targetUserName = targetUserName,
                onToggleAudio = { callViewModel.toggleAudio() },
                onToggleVideo = { callViewModel.toggleVideo() },
                onSwitchCamera = { callViewModel.switchCamera() },
                onEndCall = {
                    callViewModel.endCall()
                    navController.popBackStack()
                }
            )
        }
        // Estado: Erro (apenas para erros que NÃO sejam offline)
        else if (callState is CallState.Error) {
            val errorMsg = (callState as CallState.Error).message
            if (!errorMsg.contains("offline", ignoreCase = true)) {
                ErrorScreen(
                    message = errorMsg,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }

        // Snackbar para mensagens (usuário offline)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFFFFA726),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun CallingScreen(
    targetUserName: String,
    callType: String,
    pulseScale: Float,
    localStream: org.webrtc.MediaStream?,
    eglBase: EglBase,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF388E3C)
                    )
                )
            )
    ) {
        // Vídeo local de fundo (preview enquanto aguarda)
        if (callType == "video" && localStream != null) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        init(eglBase.eglBaseContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setEnableHardwareScaler(true)
                        setZOrderMediaOverlay(false)
                        setMirror(true)
                        localStream.videoTracks?.firstOrNull()?.addSink(this)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Avatar e info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar animado
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = targetUserName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chamando...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aguardando ${targetUserName} aceitar",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Botão Encerrar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = Color.Red,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Encerrar",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cancelar",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveCallScreen(
    eglBase: EglBase,
    localStream: org.webrtc.MediaStream?,
    remoteStream: org.webrtc.MediaStream?,
    callType: String,
    isVideoEnabled: Boolean,
    isAudioEnabled: Boolean,
    callDuration: Long,
    targetUserName: String,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    var remoteVideoView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
    var localVideoView by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    // Cleanup quando sair
    DisposableEffect(Unit) {
        onDispose {
            try {
                remoteVideoView?.release()
                localVideoView?.release()
            } catch (e: Exception) {
                // Ignorar erros no cleanup
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Vídeo remoto (tela cheia)
        if (callType == "video" && remoteStream != null) {
            AndroidView(
                factory = { ctx ->
                    SurfaceViewRenderer(ctx).apply {
                        init(eglBase.eglBaseContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setEnableHardwareScaler(true)
                        setZOrderMediaOverlay(false)
                        setMirror(false)
                        remoteVideoView = this

                        // Adicionar sink imediatamente
                        remoteStream.videoTracks?.firstOrNull()?.let { track ->
                            track.setEnabled(true)
                            track.addSink(this)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder enquanto não há vídeo remoto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B5E20)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = targetUserName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (remoteStream == null) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Conectando vídeo...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Vídeo local (miniatura)
        if (callType == "video" && isVideoEnabled && localStream != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            init(eglBase.eglBaseContext, null)
                            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setEnableHardwareScaler(true)
                            setZOrderMediaOverlay(true)
                            setMirror(true)
                            localVideoView = this

                            // Adicionar sink imediatamente
                            localStream.videoTracks?.firstOrNull()?.let { track ->
                                track.setEnabled(true)
                                track.addSink(this)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Header (info do topo)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = targetUserName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDuration(callDuration),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Controles (parte inferior)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Áudio
                ControlButton(
                    icon = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    label = "Áudio",
                    enabled = isAudioEnabled,
                    onClick = onToggleAudio
                )

                // Encerrar (maior)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FloatingActionButton(
                        onClick = onEndCall,
                        containerColor = Color.Red,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "Encerrar",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encerrar",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // Vídeo
                if (callType == "video") {
                    ControlButton(
                        icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        label = "Vídeo",
                        enabled = isVideoEnabled,
                        onClick = onToggleVideo
                    )
                }
            }
        }

        // Botão trocar câmera
        if (callType == "video") {
            FloatingActionButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = Color.White.copy(alpha = 0.3f)
            ) {
                Icon(
                    Icons.Default.Cameraswitch,
                    contentDescription = "Trocar Câmera",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (enabled) Color.White.copy(alpha = 0.3f) else Color.Red,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chamada não conectou",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text("OK", fontSize = 16.sp)
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

