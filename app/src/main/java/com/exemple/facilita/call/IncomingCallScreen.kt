package com.exemple.facilita.call

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun IncomingCallScreen(onAccept: () -> Unit, onReject: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Text("Chamada recebida")

        Row {
            Button(onClick = onAccept) {
                Text("Atender")
            }
            Button(onClick = onReject) {
                Text("Recusar")
            }
        }
    }
}
