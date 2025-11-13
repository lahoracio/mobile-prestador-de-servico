package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TelaInicio1(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "tela1")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val contentAlpha = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
        iconScale.animateTo(1f, tween(300))
        contentAlpha.animateTo(1f, tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F9F4),
                        Color.White,
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.05f),
                radius = size.maxDimension * 0.6f,
                center = Offset(size.width * 0.2f, size.height * 0.1f)
            )
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.08f),
                radius = size.maxDimension * 0.4f,
                center = Offset(size.width * 0.8f, size.height * 0.3f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..15) {
                val angle = (rotation + i * 24f) % 360f
                val radius = size.minDimension * 0.3f
                val x = center.x + cos(angle * 0.0174533f) * radius
                val y = center.y + sin(angle * 0.0174533f) * radius
                drawCircle(
                    color = Color(0xFF019D31).copy(alpha = 0.15f),
                    radius = 4f,
                    center = Offset(x, y)
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
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(iconScale.value)
                            .background(Color(0xFF019D31), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = "Bem-Vindo ao Facilita",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF019D31),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Facilitando seu dia a dia com\n segurança e praticidade",
                    fontSize = 14.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniInfoCard(
                        icon = Icons.Default.AttachMoney,
                        text = "Renda\nExtra",
                        modifier = Modifier.weight(1f)
                    )
                    MiniInfoCard(
                        icon = Icons.Default.Schedule,
                        text = "Horário\nFlexível",
                        modifier = Modifier.weight(1f)
                    )
                    MiniInfoCard(
                        icon = Icons.Default.Rocket,
                        text = "Crescimento\nRápido",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("tela_inicio2") },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF019D31)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "CONTINUAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PageIndicator(isActive = true)
                    PageIndicator(isActive = false)
                    PageIndicator(isActive = false)
                }
            }
        }

        TextButton(
            onClick = { navController.navigate("tela_login") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Pular",
                color = Color(0xFFBDBDBD),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TelaInicio2(navController: NavController) {
    val contentAlpha = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        iconScale.animateTo(1f, tween(300))
        contentAlpha.animateTo(1f, tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color.White,
                        Color(0xFFF1F9F4)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.06f),
                radius = size.maxDimension * 0.5f,
                center = Offset(size.width * 0.7f, size.height * 0.2f)
            )
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.04f),
                radius = size.maxDimension * 0.7f,
                center = Offset(size.width * 0.3f, size.height * 0.4f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            for (i in 0..8) {
                val y = size.height * i / 8f
                drawLine(
                    color = Color(0xFF019D31),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
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
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(iconScale.value)
                            .background(Color(0xFF019D31), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Controle Total",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF019D31),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "das Suas Entregas",
                    fontSize = 16.sp,

                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CleanFeatureCard(
                        icon = Icons.Default.LocationOn,
                        title = "GPS em Tempo Real",
                        subtitle = "Navegação inteligente e precisa"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.Notifications,
                        title = "Notificações Instantâneas",
                        subtitle = "Alertas de novas entregas"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.Analytics,
                        title = "Histórico de Ganhos",
                        subtitle = "Acompanhe seus rendimentos"
                    )
                }
            }

            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("tela_inicio3") },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF019D31)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "CONTINUAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PageIndicator(isActive = false)
                    PageIndicator(isActive = true)
                    PageIndicator(isActive = false)
                }
            }
        }

        TextButton(
            onClick = { navController.navigate("tela_login") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Pular",
                color = Color(0xFFBDBDBD),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TelaInicio3(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "tela3")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val contentAlpha = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        iconScale.animateTo(1f, tween(300))
        contentAlpha.animateTo(1f, tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF1F9F4),
                        Color.White,
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.08f),
                radius = size.maxDimension * 0.5f,
                center = Offset(size.width * 0.3f, size.height * 0.2f)
            )
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.05f),
                radius = size.maxDimension * 0.7f,
                center = Offset(size.width * 0.7f, size.height * 0.5f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 1..4) {
                val radius = size.minDimension * (i / 5f)
                val alpha = (5 - i) / 20f
                drawCircle(
                    color = Color(0xFF019D31).copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
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
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {

                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(iconScale.value * pulse)
                            .background(Color(0xFF019D31), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Facilitando seu dia a dia",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF019D31),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CleanFeatureCard(
                        icon = Icons.Default.Schedule,
                        title = "Horários Flexíveis",
                        subtitle = "Trabalhe quando e quanto quiser"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.MonetizationOn,
                        title = "Ganhos Crescentes",
                        subtitle = "Quanto mais fizer, mais ganha"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.Star,
                        title = "Construa Reputação",
                        subtitle = "Seja reconhecido pelo seu trabalho"
                    )
                }
            }

            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("tela_login") },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF019D31)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "COMEÇAR AGORA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PageIndicator(isActive = false)
                    PageIndicator(isActive = false)
                    PageIndicator(isActive = true)
                }
            }
        }

        TextButton(
            onClick = { navController.navigate("tela_login") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Pular",
                color = Color(0xFFBDBDBD),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PageIndicator(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(if (isActive) 40.dp else 10.dp)
            .height(4.dp)
            .background(
                color = if (isActive) Color(0xFF019D31) else Color(0xFFBDBDBD),
                shape = RoundedCornerShape(2.dp)
            )
    )
}

@Composable
fun MiniInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF019D31)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun CleanFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color(0xFF019D31).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFF019D31)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

