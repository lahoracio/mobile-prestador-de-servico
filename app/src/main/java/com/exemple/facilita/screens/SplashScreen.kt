package com.exemple.facilita.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController) {
    // Estados de animação
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Animação de rotação global
    val globalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animação de cores do gradiente
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )

    // Animação do logo
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val explosionProgress = remember { Animatable(0f) }
    val morphProgress = remember { Animatable(0f) }

    // Partículas aleatórias
    val particles = remember {
        List(60) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 2f + 1f,
                radius = Random.nextFloat() * 300f + 100f,
                size = Random.nextFloat() * 6f + 2f,
                color = if (Random.nextBoolean()) Color(0xFF00FF47) else Color(0xFF019D31)
            )
        }
    }

    // Sequência de animações
    LaunchedEffect(Unit) {
        // Fase 1: Morphing e entrada
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )

        // Fase 2: Logo aparece com bounce
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400)
        )
        logoScale.animateTo(
            targetValue = 1.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        )

        // Fase 3: Texto aparece
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600)
        )


        // Fase 4: Explosão de partículas
        explosionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )

        delay(500)

        // Navegação
        navController.navigate("tela_inicio1") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Camada 1: Gradiente de fundo dinâmico
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF019D31).copy(alpha = 0.3f + colorShift * 0.2f),
                    Color(0xFF00b14f).copy(alpha = 0.2f + colorShift * 0.15f),
                    Color(0xFF001a0d)
                ),
                center = center
            )
            drawRect(gradient)
        }

        // Camada 2: Anéis rotativos em perspectiva 3D
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(globalRotation * 0.3f)
        ) {
            val rings = 5
            for (i in 0 until rings) {
                val radius = size.minDimension * (0.15f + i * 0.12f) * morphProgress.value
                val alpha = (1f - i / rings.toFloat()) * 0.4f
                val strokeWidth = (rings - i) * 3f

                // Efeito de perspectiva
                val scaleY = 0.3f + (i / rings.toFloat()) * 0.7f

                rotate(i * 30f) {
                    drawCircle(
                        color = Color(0xFF00FF47).copy(alpha = alpha),
                        radius = radius,
                        center = Offset(center.x, center.y * scaleY),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Camada 3: Espirais rotativas
        Canvas(
            modifier = Modifier
                .size(400.dp)
                .rotate(-globalRotation * 0.5f)
                .alpha(0.6f)
        ) {
            val spirals = 3
            for (s in 0 until spirals) {
                val angleOffset = s * (360f / spirals)
                val path = Path()

                for (i in 0..100) {
                    val progress = i / 100f * morphProgress.value
                    val angle = (progress * 720f + angleOffset) * PI.toFloat() / 180f
                    val radius = progress * size.minDimension * 0.4f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius

                    if (i == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF019D31).copy(alpha = 0.5f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
        }

        // Camada 4: Partículas em explosão
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(explosionProgress.value)
        ) {
            particles.forEach { particle ->
                val explosionRadius = particle.radius * explosionProgress.value
                val angle = (particle.angle + globalRotation * particle.speed) * PI.toFloat() / 180f
                val x = center.x + cos(angle) * explosionRadius
                val y = center.y + sin(angle) * explosionRadius

                // Partícula com brilho
                drawCircle(
                    color = particle.color.copy(alpha = 1f - explosionProgress.value * 0.5f),
                    radius = particle.size,
                    center = Offset(x, y)
                )

                // Rastro da partícula
                val trailLength = 20f * explosionProgress.value
                val trailX = center.x + cos(angle) * (explosionRadius - trailLength)
                val trailY = center.y + sin(angle) * (explosionRadius - trailLength)

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            particle.color.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        start = Offset(trailX, trailY),
                        end = Offset(x, y)
                    ),
                    start = Offset(trailX, trailY),
                    end = Offset(x, y),
                    strokeWidth = particle.size * 0.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Camada 5: Círculos pulsantes de fundo do logo
        Canvas(
            modifier = Modifier
                .size(250.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value * 0.5f)
        ) {
            for (i in 1..6) {
                val radius = size.minDimension / 2 * (i / 6f)
                val pulseOffset = sin((globalRotation + i * 60f) * PI.toFloat() / 180f) * 10f

                drawCircle(
                    color = Color(0xFF019D31).copy(alpha = (7 - i) / 12f),
                    radius = radius + pulseOffset,
                    style = Stroke(width = 2f)
                )
            }
        }

        // Camada 6: Logo central com efeito glassmorphism
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
        ) {
            // Círculo principal do logo
            Canvas(modifier = Modifier.size(160.dp)) {
                // Brilho externo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00FF47).copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        radius = size.minDimension / 1.5f
                    )
                )

                // Círculo glassmorphism
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF019D31).copy(alpha = 0.8f),
                            Color(0xFF00b14f).copy(alpha = 0.6f),
                            Color(0xFF019D31).copy(alpha = 0.9f)
                        )
                    ),
                    radius = size.minDimension / 2.5f
                )

                // Borda externa brilhante
                drawCircle(
                    color = Color(0xFF00FF47),
                    radius = size.minDimension / 2.5f,
                    style = Stroke(width = 4f)
                )

                // Borda interna
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = size.minDimension / 3f,
                    style = Stroke(width = 2f)
                )

                // Detalhes internos
                rotate(globalRotation) {
                    for (i in 0 until 4) {
                        val angle = i * 90f
                        rotate(angle) {
                            val startY = -size.minDimension / 6f
                            val endY = -size.minDimension / 4.5f
                            drawLine(
                                color = Color(0xFF00FF47).copy(alpha = 0.6f),
                                start = Offset(center.x, center.y + startY),
                                end = Offset(center.x, center.y + endY),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Texto com efeito neon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                // Sombra/brilho do texto
                Text(
                    text = "Facilita",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00FF47),
                    letterSpacing = 4.sp,
                    modifier = Modifier
                        .blur(12.dp)
                        .alpha(0.7f)
                )

                // Texto principal
                Text(
                    text = "Facilita",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp,
                    modifier = Modifier.offset(y = (-52).dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Badge "PRESTADOR"
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF00FF47),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "PRESTADOR",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subtítulo com animação
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color(0xFF00FF47),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seu trabalho facilita vidas. ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00FF47),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color(0xFF00FF47),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }

        // Camada 7: Efeito de luz inferior
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .alpha(0.4f)
        ) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF019D31).copy(alpha = 0.3f),
                        Color(0xFF00FF47).copy(alpha = 0.2f)
                    )
                )
            )
        }
    }
}

data class Particle(
    val angle: Float,
    val speed: Float,
    val radius: Float,
    val size: Float,
    val color: Color
)

