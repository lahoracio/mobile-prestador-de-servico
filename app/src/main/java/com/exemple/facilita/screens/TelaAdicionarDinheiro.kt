package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.viewmodel.CarteiraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAdicionarDinheiro(navController: NavController) {
    val viewModel = viewModel<CarteiraViewModel>()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var valor by remember { mutableStateOf("") }
    var metodoPagamento by remember { mutableStateOf("PIX") }

    val infiniteTransition = rememberInfiniteTransition(label = "adicionar")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Dinheiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F9F4),
                            Color.White
                        )
                    )
                )
                .padding(padding)
        ) {

            // Efeitos de fundo
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..15) {
                    val offsetX = (size.width / 15f) * i
                    val offsetY = size.height * ((shimmer + i * 0.07f) % 1f)
                    val alpha = kotlin.math.abs(kotlin.math.sin((shimmer + i * 0.1f) * 3.14f)) * 0.15f

                    drawCircle(
                        color = Color(0xFF019D31).copy(alpha = alpha),
                        radius = 10f,
                        center = Offset(offsetX, offsetY)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Card Informativo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF019D31).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF019D31),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Adicione créditos à sua carteira para começar a realizar transações",
                            fontSize = 14.sp,
                            color = Color(0xFF019D31),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Campo de Valor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Quanto deseja adicionar?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF019D31)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = valor,
                            onValueChange = { valor = it },
                            label = { Text("Valor (R$)") },
                            placeholder = { Text("0,00") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF019D31),
                                focusedLabelColor = Color(0xFF019D31),
                                cursorColor = Color(0xFF019D31)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Valores sugeridos
                        Text(
                            text = "Valores sugeridos:",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50.0, 100.0, 200.0, 500.0).forEach { value ->
                                FilterChip(
                                    selected = valor == value.toString(),
                                    onClick = { valor = value.toString() },
                                    label = {
                                        Text(
                                            "R$ ${value.toInt()}",
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF019D31),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Método de Pagamento
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Método de Pagamento",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF019D31)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        listOf(
                            "PIX" to Icons.Default.QrCode,
                            "Boleto" to Icons.Default.Receipt,
                            "Cartão de Crédito" to Icons.Default.CreditCard
                        ).forEach { (metodo, icon) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (metodoPagamento == metodo)
                                        Color(0xFF019D31).copy(alpha = 0.1f)
                                    else Color(0xFFF5F5F5)
                                ),
                                onClick = { metodoPagamento = metodo }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (metodoPagamento == metodo)
                                                Color(0xFF019D31)
                                            else Color(0xFF757575)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = metodo,
                                            fontSize = 16.sp,
                                            fontWeight = if (metodoPagamento == metodo)
                                                FontWeight.Bold
                                            else FontWeight.Normal,
                                            color = if (metodoPagamento == metodo)
                                                Color(0xFF019D31)
                                            else Color(0xFF424242)
                                        )
                                    }

                                    if (metodoPagamento == metodo) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF019D31)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Botão Confirmar
                Button(
                    onClick = {
                        val valorDouble = valor.replace(",", ".").toDoubleOrNull()
                        if (valorDouble != null && valorDouble > 0) {
                            viewModel.solicitarDeposito(valorDouble, "dummy_token") {
                                // Navegar para tela do QR Code
                                navController.navigate("tela_qrcode_pix/$valorDouble")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && valor.toDoubleOrNull() != null && valor.toDouble() > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF019D31)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Confirmar Adição",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Mensagens
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.limparMensagens() }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFF44336)
                ) {
                    Text(message, color = Color.White)
                }
            }

            successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(message, color = Color.White)
                    }
                }
            }
        }
    }
}

