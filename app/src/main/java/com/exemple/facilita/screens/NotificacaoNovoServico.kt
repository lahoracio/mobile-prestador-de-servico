package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.exemple.facilita.model.*
import com.exemple.facilita.service.AceitarServicoApiResponse
import com.exemple.facilita.service.RetrofitFactory
import com.exemple.facilita.utils.TokenManager
import com.exemple.facilita.viewmodel.ServicoViewModel
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Nota: A função Servico.toServicoDetalhe() está definida em TelaInicioPrestador.kt
// e é acessível aqui pois ambas estão no mesmo pacote (com.exemple.facilita.screens)

@Composable
fun NotificacaoNovoServico(
    servico: Servico,
    tempoRestante: Int,
    onAceitar: () -> Unit,
    onVoltar: () -> Unit,
    navController: NavController,
    servicoViewModel: ServicoViewModel
) {
    val context = LocalContext.current
    val token = TokenManager.obterTokenComBearer(context) ?: ""
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Animação de pulso para o timer
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

    fun aceitarServico() {
        if (isLoading) return
        isLoading = true

        val service = RetrofitFactory.getServicoService()
        service.aceitarServico(token, servico.id).enqueue(object : Callback<AceitarServicoApiResponse> {
            override fun onResponse(
                call: Call<AceitarServicoApiResponse>,
                response: Response<AceitarServicoApiResponse>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    val servicoAceito = response.body()?.data
                    if (servicoAceito != null) {
                        // Converter e salvar usando a extension function
                        val servicoDetalhe = servicoAceito.toServicoDetalhe()
                        servicoViewModel.salvarServicoAceito(servicoDetalhe)

                        showSuccessDialog = true

                        // Navegar após 1 segundo
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            onAceitar()
                            navController.navigate("tela_detalhes_servico_aceito/${servicoDetalhe.id}")
                        }, 1000)
                    }
                } else {
                    Toast.makeText(context, "Erro ao aceitar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AceitarServicoApiResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Erro de conexão: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onVoltar() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.75f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Texto no topo
                    Text(
                        text = "aceitação do serviço",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Timer circular
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Círculo de fundo
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                            )

                            // Timer
                            Text(
                                text = "${tempoRestante}s",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Borda pulsante
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .then(
                                        Modifier.background(
                                            Color.White.copy(alpha = pulseAlpha * 0.3f),
                                            CircleShape
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Título
                        Text(
                            text = "Aceitar Serviço?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Valor
                        Text(
                            text = "R$ ${servico.valor}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Informações
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Distância
                            InfoItem(
                                icon = Icons.Default.Navigation,
                                text = "10 km" // Pode calcular distância real
                            )

                            // Círculo separador
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.CenterVertically)
                            )

                            // Categoria
                            InfoItem(
                                icon = Icons.Default.Build,
                                text = servico.categoria.nome
                            )

                            // Círculo separador
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .align(Alignment.CenterVertically)
                            )

                            // Local
                            InfoItem(
                                icon = Icons.Default.LocationOn,
                                text = servico.localizacao?.cidade ?: "Não informado"
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Botão Aceitar
                        Button(
                            onClick = { if (!isLoading) aceitarServico() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.9f),
                                disabledContainerColor = Color.White.copy(alpha = 0.5f)
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF4CAF50),
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Aceitar",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão Voltar
                        TextButton(
                            onClick = { if (!isLoading) onVoltar() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Voltar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog de sucesso
    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(modifier = Modifier.padding(48.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sucesso",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "Serviço Aceito!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1
        )
    }
}

