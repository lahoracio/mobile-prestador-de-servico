package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun TelaInicioWelcome(navController: NavController) {
    // Animações
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    // Animações de entrada
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(50f) }
    val cardsAlpha = remember { Animatable(0f) }
    val cardsScale = remember { Animatable(0.8f) }
    val buttonAlpha = remember { Animatable(0f) }
    val buttonScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Sequência de animações
        titleAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        titleOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing))

        delay(200)
        cardsAlpha.animateTo(1f, tween(600))
        cardsScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

        delay(300)
        buttonAlpha.animateTo(1f, tween(600))
        buttonScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF001a0d),
                        Color(0xFF003d1a),
                        Color(0xFF001a0d)
                    )
                )
            )
    ) {
        // Partículas de fundo animadas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val particles = 20
            for (i in 0 until particles) {
                val offsetX = (size.width / particles) * i
                val offsetY = size.height * ((shimmer + i * 0.1f) % 1f)
                val alpha = sin((shimmer * 3.14f * 2 + i) % (3.14f * 2)) * 0.3f + 0.2f

                drawCircle(
                    color = Color(0xFF00FF47).copy(alpha = alpha),
                    radius = 4f,
                    center = Offset(offsetX, offsetY)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Seção superior: Título e subtítulo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp)
            ) {
                // Logo/Ícone
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFF00FF47).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF00FF47)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = "Bem-vindo ao",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Facilita Prestador",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00FF47),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Transforme seu tempo em dinheiro\nfazendo entregas pela cidade",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            // Seção do meio: Cards de benefícios
            Column(
                modifier = Modifier
                    .alpha(cardsAlpha.value)
                    .scale(cardsScale.value),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BenefitCard(
                    icon = Icons.Default.AttachMoney,
                    title = "Ganhe Mais",
                    description = "Defina seus próprios horários e ganhe por entrega",
                    color = Color(0xFF00FF47)
                )

                BenefitCard(
                    icon = Icons.Default.Schedule,
                    title = "Flexibilidade Total",
                    description = "Trabalhe quando e onde quiser, no seu tempo",
                    color = Color(0xFF4CAF50)
                )

                BenefitCard(
                    icon = Icons.Default.Star,
                    title = "Seja Valorizado",
                    description = "Construa sua reputação e receba mais ofertas",
                    color = Color(0xFF8BC34A)
                )
            }

            // Seção inferior: Botões
            Column(
                modifier = Modifier
                    .alpha(buttonAlpha.value)
                    .scale(buttonScale.value),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão principal - Criar conta
                Button(
                    onClick = {
                        navController.navigate("tela_cadastro") {
                            popUpTo("tela_inicio_welcome") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF47)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Text(
                        text = "Criar Conta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Botão secundário - Já tenho conta
                OutlinedButton(
                    onClick = {
                        navController.navigate("tela_login") {
                            popUpTo("tela_inicio_welcome") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00FF47)
                    ),
                    border = ButtonStroke(2.dp, Color(0xFF00FF47)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Já tenho conta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Efeito de brilho no topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FF47).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun BenefitCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Classe auxiliar para BorderStroke
private fun ButtonStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

