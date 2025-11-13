package com.exemple.facilita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.exemple.facilita.viewmodel.CarteiraViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.*

/**
 * Tela para exibir QR Code PIX gerado pelo PagBank
 * Mostra o QR Code, código copia e cola e status do pagamento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaQRCodePix(
    navController: NavController,
    valor: Double
) {
    val viewModel: CarteiraViewModel = viewModel()
    val qrCodeData by viewModel.qrCodePix.collectAsState()
    val pixCopiaCola by viewModel.pixCopiaCola.collectAsState()
    val chargeId by viewModel.chargeId.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    var tempoRestante by remember { mutableStateOf(600) } // 10 minutos
    var statusPagamento by remember { mutableStateOf("AGUARDANDO") }

    // Timer de expiração
    LaunchedEffect(Unit) {
        while (tempoRestante > 0 && statusPagamento == "AGUARDANDO") {
            delay(1000)
            tempoRestante--

            // Consultar status a cada 5 segundos
            if (tempoRestante % 5 == 0 && chargeId != null) {
                viewModel.consultarStatusPix(chargeId!!)
            }
        }
    }

    // Monitorar pagamento confirmado
    LaunchedEffect(successMessage) {
        if (successMessage?.contains("confirmado") == true) {
            statusPagamento = "PAGO"
            delay(2000)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagamento via PIX", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF019D31),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF1F9F4), Color.White)))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Card de Valor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF019D31))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Valor a Pagar",
                            fontSize = 16.sp,
                            color = Color.White.copy(0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            numberFormat.format(valor),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Status
                if (statusPagamento == "PAGO") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Pagamento Confirmado!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Timer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Timer, null, tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Expira em: ${tempoRestante / 60}:${(tempoRestante % 60).toString().padStart(2, '0')}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }

                // QR Code
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Escaneie o QR Code",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF019D31)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Image
                        qrCodeData?.links?.find { it.media == "image/png" }?.href?.let { qrUrl ->
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(2.dp, Color(0xFF019D31), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(qrUrl),
                                    contentDescription = "QR Code PIX",
                                    modifier = Modifier
                                        .size(260.dp)
                                        .padding(8.dp)
                                )
                            }
                        } ?: run {
                            // Placeholder enquanto carrega
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF019D31))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Abra o app do seu banco e escaneie o código",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Copia e Cola
                pixCopiaCola?.let { codigo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                "ou copie o código PIX",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF019D31)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    codigo.take(50) + "...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF424242),
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(codigo))
                                    // Mostrar toast
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF019D31)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar Código PIX")
                            }
                        }
                    }
                }

                // Instruções
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Como pagar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        listOf(
                            "1. Abra o app do seu banco",
                            "2. Escolha pagar com PIX",
                            "3. Escaneie o QR Code ou cole o código",
                            "4. Confirme o pagamento",
                            "5. Aguarde a confirmação automática"
                        ).forEach { instrucao ->
                            Text(
                                instrucao,
                                fontSize = 14.sp,
                                color = Color(0xFF424242),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

