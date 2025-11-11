package com.exemple.facilita.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.exemple.facilita.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@Composable
fun TelaPermissaoLocalizacaoServico(navController: NavController) {
    val context = LocalContext.current
    val locationSettingsClient = remember { LocationServices.getSettingsClient(context) }

    // Launcher para abrir prompt de ativar GPS
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // GPS ativado, navegar para próxima tela
            navController.navigate("tela_tipo_veiculo")
        } else {
            Toast.makeText(context, "GPS não ativado", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para solicitar permissões de localização
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            // Após aceitar permissões, ativar GPS e navegar
            ativarGPS(
                context = context,
                locationSettingsClient = locationSettingsClient,
                onGPSAtivado = { navController.navigate("tela_tipo_veiculo") },
                launcher = locationLauncher
            )
        } else {
            Toast.makeText(
                context,
                "Permissões de localização são necessárias para continuar",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Botão de voltar
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowLeft,
                contentDescription = "Voltar",
                tint = Color(0xFF444444)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Texto
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 60.dp)) {
                Text(
                    text = "Seja bem-vindo!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Precisamos da sua localização para comunicar os clientes que você está chegando. " +
                            "A localização precisa estar ativa o tempo todo nas permissões do app.",
                    fontSize = 15.sp,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center
                )
            }

            // Imagem
            Box(modifier = Modifier.offset(y = (-40).dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.sejabemvindo),
                    contentDescription = "Ilustração de localização",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Botões
            Column(modifier = Modifier.fillMaxWidth().offset(y = (-50).dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Negar",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(48.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF015B2B), Color(0xFF00B94A))
                                ),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Permitir",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// Função para ativar GPS
private fun ativarGPS(
    context: Context,
    locationSettingsClient: SettingsClient,
    onGPSAtivado: () -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    val task = locationSettingsClient.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        // GPS já ativado → navegar
        onGPSAtivado()
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            // GPS não ativado → abre prompt
            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
            launcher.launch(intentSenderRequest)
        } else {
            Toast.makeText(context, "Erro ao verificar GPS", Toast.LENGTH_SHORT).show()
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TelaPermissaoLocalizacaoServicoPreview() {
    val navController = rememberNavController()
    TelaPermissaoLocalizacaoServico(navController = navController)
}
