package com.exemple.facilita.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IncomingCallScreen(
    callerName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Chamada de $callerName")

        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Atender") }

        Button(
            onClick = onReject,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Recusar") }
    }
}
