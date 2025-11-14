package com.exemple.facilita.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhePedido(
    navController: NavController,
    servicoId: Int,
    clienteNome: String,
    servicoDescricao: String,
    valor: String,
    local: String,
    horario: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var animateCard by remember { mutableStateOf(false) }
    var animateButtons by remember { mutableStateOf(false) }
    var servicoStatus by remember { mutableStateOf("Aguardando") }

    // Cores do tema
    val primaryGreen = Color(0xFF2E7D32)
    val secondaryGreen = Color(0xFF388E3C)
    val accentGreen = Color(0xFF4CAF50)
    val lightBg = Color(0xFFF5F5F5)
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)

    // Animações de entrada
    LaunchedEffect(Unit) {
        delay(100)
        animateCard = true
        delay(200)
        animateButtons = true
        delay(500)
        showMap = true
    }

    // Animação de pulso para status
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Scaffold(
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Detalhes do Serviço",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textPrimary
                        )
                        Text(
                            "ID: #$servicoId",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAFAFA), Color(0xFFF5F5F5))
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card de Status com animação
                AnimatedVisibility(
                    visible = animateCard,
                    enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(primaryGreen, secondaryGreen, accentGreen)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Status do Serviço",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        servicoStatus,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = pulseAlpha * 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Card do Cliente
                AnimatedVisibility(
                    visible = animateCard,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(primaryGreen, accentGreen)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Cliente",
                                        color = textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        clienteNome,
                                        color = textPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Botões de ação
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { /* Ligar */ },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(accentGreen.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "Ligar",
                                            tint = primaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { /* Chat */ },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(accentGreen.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = "Chat",
                                            tint = primaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Card de Detalhes do Serviço
                AnimatedVisibility(
                    visible = animateCard,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Detalhes do Serviço",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Serviço
                            DetailRow(
                                icon = Icons.Default.Build,
                                label = "Serviço",
                                value = servicoDescricao,
                                primaryColor = primaryGreen,
                                textSecondary = textSecondary,
                                textPrimary = textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Localização
                            DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Local",
                                value = local,
                                primaryColor = primaryGreen,
                                textSecondary = textSecondary,
                                textPrimary = textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Horário
                            DetailRow(
                                icon = Icons.Default.Schedule,
                                label = "Horário",
                                value = horario,
                                primaryColor = primaryGreen,
                                textSecondary = textSecondary,
                                textPrimary = textPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Valor
                            DetailRow(
                                icon = Icons.Default.AttachMoney,
                                label = "Valor",
                                value = valor,
                                primaryColor = primaryGreen,
                                textSecondary = textSecondary,
                                textPrimary = textPrimary,
                                isHighlight = true
                            )
                        }
                    }
                }

                // Mapa (Placeholder com animação)
                AnimatedVisibility(
                    visible = showMap,
                    enter = fadeIn() + expandVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFE8F5E9),
                                            Color(0xFFC8E6C9)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Mapa de Localização",
                                    color = primaryGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "A rota será exibida aqui",
                                    color = textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Botão flutuante de Iniciar Deslocamento
            AnimatedVisibility(
                visible = animateButtons,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                Toast.makeText(
                                    context,
                                    "Iniciando deslocamento...",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Aqui você chamaria a API para iniciar o deslocamento
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp,
                                    color = Color.White
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        "Iniciar Deslocamento",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { navController.navigateUp() },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textSecondary
                            ),
                            border = BorderStroke(
                                2.dp,
                                textSecondary.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                "Cancelar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    primaryColor: Color,
    textSecondary: Color,
    textPrimary: Color,
    isHighlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .then(
                    if (isHighlight)
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor, Color(0xFF4CAF50))
                            )
                        )
                    else
                        Modifier.background(primaryColor.copy(alpha = 0.15f))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) Color.White else primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                color = textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                color = if (isHighlight) primaryColor else textPrimary,
                fontSize = if (isHighlight) 20.sp else 16.sp,
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
                lineHeight = 22.sp
            )
        }
    }
}

