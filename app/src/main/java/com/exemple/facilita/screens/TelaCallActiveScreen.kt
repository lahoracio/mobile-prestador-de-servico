package com.exemple.facilita.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

@Composable
fun CallActiveScreen(
    localView: SurfaceViewRenderer,
    remoteView: SurfaceViewRenderer,
    onEnd: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {

        AndroidView(
            { remoteView },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        AndroidView(
            { localView },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        Button(
            onClick = onEnd,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Encerrar chamada")
        }
    }
}