package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.CallType
import com.exemple.facilita.model.IncomingCallData
import com.exemple.facilita.viewmodel.CallViewModel

// Composable que pode ser usado tanto como tela quanto como dialog
@Composable
fun TelaIncomingCall(
    navController: NavController,
    servicoId: Int,
    callerId: Int,
    callerName: String,
    callType: String,
    callId: String,
    userId: Int,
    userName: String,
    callViewModel: CallViewModel = viewModel()
) {
    IncomingCallContent(
        servicoId = servicoId,
        callerId = callerId,
        callerName = callerName,
        callType = callType,
        callId = callId,
        onAccept = {
            callViewModel.acceptCall(IncomingCallData(
                servicoId = servicoId,
                callerId = callerId,
                callerName = callerName,
                callType = if (callType == "video") CallType.VIDEO else CallType.AUDIO,
                callId = callId,
                timestamp = System.currentTimeMillis()
            ))
            navController.popBackStack()
        },
        onReject = {
            callViewModel.rejectCall(IncomingCallData(
                servicoId = servicoId,
                callerId = callerId,
                callerName = callerName,
                callType = if (callType == "video") CallType.VIDEO else CallType.AUDIO,
                callId = callId,
                timestamp = System.currentTimeMillis()
            ))
            navController.popBackStack()
        }
    )
}

// Dialog para chamada recebida (aparece por cima sem destruir a tela atual)
@Composable
fun IncomingCallDialog(
    incomingCallData: IncomingCallData,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Não permite fechar clicando fora */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        IncomingCallContent(
            servicoId = incomingCallData.servicoId,
            callerId = incomingCallData.callerId,
            callerName = incomingCallData.callerName,
            callType = if (incomingCallData.callType == CallType.VIDEO) "video" else "audio",
            callId = incomingCallData.callId,
            onAccept = onAccept,
            onReject = onReject
        )
    }
}

// Conteúdo visual da tela/dialog de chamada recebida
@Composable
private fun IncomingCallContent(
    servicoId: Int,
    callerId: Int,
    callerName: String,
    callType: String,
    callId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    // Animação de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Ícone e info da chamada
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone animado
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (callType == "video")
                            Icons.Default.Videocam
                        else
                            Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Chamada de ${if (callType == "video") "vídeo" else "áudio"}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = callerName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Serviço #$servicoId",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // Botões de ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rejeitar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onReject,
                        containerColor = Color.Red,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Rejeitar",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rejeitar",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Aceitar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FloatingActionButton(
                        onClick = onAccept,
                        containerColor = Color.Green,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Aceitar",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aceitar",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

