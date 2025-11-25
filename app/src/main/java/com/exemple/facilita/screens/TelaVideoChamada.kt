// TelaVideoChamada.kt - ATUALIZADA
package com.exemple.facilita.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.exemple.facilita.call.CallViewModel
import com.exemple.facilita.webrtc.WebRtcModule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd

@Composable
fun TelaVideoChamada(
    navController: NavHostController,
    servicoId: Int,
    callerId: Int,
    callerName: String,
    callViewModel: CallViewModel
) {
    val context = LocalContext.current

    DisposableEffect(context) {
        // Inicializa WebRTC com as views
        callViewModel.initWebRTC(WebRtcModule.localView, WebRtcModule.remoteView)

        onDispose {
            WebRtcModule.cleanup()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Vídeo remoto
        AndroidView(
            factory = { WebRtcModule.remoteView },
            modifier = Modifier.fillMaxSize()
        )

        // Vídeo local pequeno
        AndroidView(
            factory = { WebRtcModule.localView },
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Botão para encerrar chamada
        FloatingActionButton(
            onClick = {
                WebRtcModule.endCall()
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.CallEnd, "Encerrar chamada")
        }
    }
}