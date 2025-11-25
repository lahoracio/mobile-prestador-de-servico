// TelaIniciarChamada.kt - ATUALIZADA
package com.exemple.facilita.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.exemple.facilita.call.CallViewModel
import com.exemple.facilita.data.service.WebSocketService
import com.exemple.facilita.webrtc.WebRtcModule

@Composable
fun TelaIniciarChamada(
    navController: NavHostController,
    servicoId: Int,
    callerId: Int,
    callerName: String,
    targetId: Int,
    targetName: String,
    webSocketService: WebSocketService,
    callViewModel: CallViewModel
) {

    LaunchedEffect(Unit) {
        // Inicia a chamada
        callViewModel.startCall(servicoId, callerId, callerName, targetId)

        // Configura listeners
        WebRtcModule.onCallAccepted = {
            // Quando a chamada é aceita, navega para a tela de vídeo
            navController.navigate("tela_video_chamada/$servicoId/$callerId/$callerName")
        }

        WebRtcModule.onCallRejected = {
            // Quando a chamada é rejeitada, volta para tela anterior
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}