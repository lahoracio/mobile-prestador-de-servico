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
import kotlin.math.abs

@Composable
fun TelaInicio1(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "tela1")

    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    val greenPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
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
                color = Color(0xFF06C755).copy(alpha = 0.08f * greenPulse),
                radius = size.maxDimension * 0.5f,
                center = Offset(size.width * 0.7f, size.height * 0.2f)
            )
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.06f * greenPulse),
                radius = size.maxDimension * 0.7f,
                center = Offset(size.width * 0.3f, size.height * 0.4f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..20) {
                val xOffset = (size.width / 20f) * i
                val yProgress = (particleOffset + i * 0.05f) % 1f
                val yPos = size.height * yProgress
                val alpha = if (yProgress < 0.5f) yProgress else (1f - yProgress)

                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = alpha * 0.4f),
                    radius = 6f + (i % 3) * 2f,
                    center = Offset(xOffset, yPos)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
            for (i in 0..8) {
                val y = size.height * i / 8f
                drawLine(
                    color = Color(0xFF06C755),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f
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
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawCircle(
                            color = Color(0xFF06C755).copy(alpha = greenPulse * 0.3f),
                            radius = size.minDimension / 2f
                        )
                    }

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

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Bem-Vindo Ao Facilita",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF019D31),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CleanFeatureCard(
                        icon = Icons.Default.AttachMoney,
                        title = "Renda Extra",
                        subtitle = "Ganhe dinheiro no seu tempo livre"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.Schedule,
                        title = "Horário Flexível",
                        subtitle = "Trabalhe quando quiser"
                    )
                    CleanFeatureCard(
                        icon = Icons.Default.Star,
                        title = "Crescimento Rápido",
                        subtitle = "Construa sua reputação"
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
    val infiniteTransition = rememberInfiniteTransition(label = "tela2")

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waves"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
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
                        Color(0xFFE8F5E9),
                        Color.White,
                        Color(0xFFF1F9F4)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..4) {
                val waveRadius = (size.minDimension * 0.3f) * ((waveProgress + i * 0.2f) % 1f)
                val waveAlpha = 1f - ((waveProgress + i * 0.2f) % 1f)

                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = waveAlpha * 0.3f),
                    radius = waveRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val angle1 = rotation * 0.0174533f
            val angle2 = (rotation + 120f) * 0.0174533f
            val angle3 = (rotation + 240f) * 0.0174533f
            val radius = size.minDimension * 0.25f

            drawCircle(
                color = Color(0xFF06C755).copy(alpha = 0.1f),
                radius = size.maxDimension * 0.15f,
                center = Offset(
                    center.x + cos(angle1) * radius,
                    center.y + sin(angle1) * radius
                )
            )
            drawCircle(
                color = Color(0xFF019D31).copy(alpha = 0.08f),
                radius = size.maxDimension * 0.12f,
                center = Offset(
                    center.x + cos(angle2) * radius,
                    center.y + sin(angle2) * radius
                )
            )
            drawCircle(
                color = Color(0xFF06C755).copy(alpha = 0.06f),
                radius = size.maxDimension * 0.18f,
                center = Offset(
                    center.x + cos(angle3) * radius,
                    center.y + sin(angle3) * radius
                )
            )
        }

        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            for (i in 0..8) {
                val y = size.height * i / 8f
                drawLine(
                    color = Color(0xFF06C755),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f
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
                    Canvas(modifier = Modifier.size(140.dp)) {
                        for (i in 1..3) {
                            val ringRadius = size.minDimension / 2f * (0.8f + i * 0.1f)
                            val ringAlpha = (1f - (waveProgress + i * 0.3f) % 1f) * 0.4f

                            drawCircle(
                                color = Color(0xFF06C755).copy(alpha = ringAlpha),
                                radius = ringRadius,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }
                    }

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

                Spacer(modifier = Modifier.height(20.dp))

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

    val explosionProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "explosion"
    )

    val sparkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
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
            for (i in 0..30) {
                val angle = (i * 12f) * 0.0174533f
                val distance = size.minDimension * 0.35f * explosionProgress
                val particleX = center.x + cos(angle) * distance
                val particleY = center.y + sin(angle) * distance
                val particleAlpha = (1f - explosionProgress) * 0.6f

                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = particleAlpha),
                    radius = 8f - (explosionProgress * 4f),
                    center = Offset(particleX, particleY)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 1..5) {
                val radius = size.minDimension * (i / 6f) * pulse
                val alpha = ((6 - i) / 15f) * sparkle
                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = alpha),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val starPositions = listOf(
                Offset(size.width * 0.15f, size.height * 0.15f),
                Offset(size.width * 0.85f, size.height * 0.15f),
                Offset(size.width * 0.15f, size.height * 0.85f),
                Offset(size.width * 0.85f, size.height * 0.85f),
                Offset(size.width * 0.5f, size.height * 0.1f)
            )

            starPositions.forEachIndexed { index, pos ->
                val starAlpha = abs(sin((sparkle + index * 0.2f) * 3.14f)) * 0.5f
                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = starAlpha),
                    radius = 6f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF06C755).copy(alpha = starAlpha * 0.3f),
                    radius = 12f,
                    center = pos
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
                    Canvas(modifier = Modifier.size(160.dp)) {
                        drawCircle(
                            color = Color(0xFF06C755).copy(alpha = sparkle * 0.3f),
                            radius = size.minDimension / 2f
                        )
                        for (i in 0..7) {
                            val angle = (i * 45f + explosionProgress * 360f) * 0.0174533f
                            val startRadius = size.minDimension * 0.3f
                            val endRadius = size.minDimension * 0.5f
                            val rayAlpha = sparkle * 0.4f

                            drawLine(
                                color = Color(0xFF06C755).copy(alpha = rayAlpha),
                                start = Offset(
                                    center.x + cos(angle) * startRadius,
                                    center.y + sin(angle) * startRadius
                                ),
                                end = Offset(
                                    center.x + cos(angle) * endRadius,
                                    center.y + sin(angle) * endRadius
                                ),
                                strokeWidth = 3f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }

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
                    text = "Comece Agora",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF019D31),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Facilitando seu dia a dia",
                    fontSize = 16.sp,
                    color = Color(0xFF424242),
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

