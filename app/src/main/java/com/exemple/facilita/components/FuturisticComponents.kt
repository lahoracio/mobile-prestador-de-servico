package com.exemple.facilita.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Badge futurista com animação de pulso
 */
@Composable
fun FuturisticBadge(
    text: String,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0xFF141B2D),
    accentColor: Color = Color(0xFF00FF88),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Card com borda animada em gradiente
 */
@Composable
fun NeonBorderCard(
    modifier: Modifier = Modifier,
    borderWidth: Dp = 2.dp,
    cornerRadius: Dp = 20.dp,
    primaryColor: Color = Color(0xFF00FF88),
    secondaryColor: Color = Color(0xFF00D4FF),
    backgroundColor: Color = Color(0xFF141B2D),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.sweepGradient(
                    0f to primaryColor,
                    0.5f to secondaryColor,
                    1f to primaryColor
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(borderWidth)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(cornerRadius - 1.dp))
                .padding(20.dp),
            content = content
        )
    }
}

/**
 * Linha divisória futurística
 */
@Composable
fun FuturisticDivider(
    color: Color = Color(0xFF00FF88),
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = 0.5f),
                        color,
                        color.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Ícone circular com gradiente
 */
@Composable
fun GradientIconCircle(
    icon: ImageVector,
    size: Dp = 56.dp,
    iconSize: Dp = 28.dp,
    startColor: Color = Color(0xFF00FF88),
    endColor: Color = Color(0xFF00D4FF),
    iconTint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor)
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Título de seção futurístico
 */
@Composable
fun FuturisticSectionTitle(
    text: String,
    icon: ImageVector? = null,
    accentColor: Color = Color(0xFF00FF88),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = text.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 2.sp
        )
    }
}

/**
 * Botão com efeito de brilho
 */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    glowColor: Color = Color(0xFF00FF88),
    textColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier = modifier) {
        // Brilho de fundo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .alpha(glowAlpha)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0f),
                            glowColor.copy(alpha = 0.3f),
                            glowColor.copy(alpha = 0f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
        )

        // Botão principal
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00B359),
                                Color(0xFF00FF88)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Text(
                        text = text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Indicador de progresso circular futurístico
 */
@Composable
fun FuturisticCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    primaryColor: Color = Color(0xFF00FF88),
    backgroundColor: Color = Color(0xFF1E2740),
    size: Dp = 120.dp,
    showPercentage: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val radius = (size.toPx() - strokeWidth.toPx()) / 2

            // Círculo de fundo
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Arco de progresso
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        Color(0xFF00D4FF),
                        primaryColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(
                    strokeWidth.toPx() / 2,
                    strokeWidth.toPx() / 2
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidth.toPx(),
                    size.toPx() - strokeWidth.toPx()
                )
            )
        }

        if (showPercentage) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

/**
 * Card de informação com ícone lateral
 */
@Composable
fun InfoCardWithIcon(
    icon: ImageVector,
    title: String,
    value: String,
    iconColor: Color = Color(0xFF00FF88),
    backgroundColor: Color = Color(0xFF141B2D),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    iconColor.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0B8C8),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Efeito de partículas de fundo
 */
@Composable
fun ParticleBackground(
    particleCount: Int = 20,
    particleColor: Color = Color(0xFF00FF88).copy(alpha = 0.3f),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(particleCount) { index ->
            val progress = (index.toFloat() / particleCount) * 360f
            val x = size.width / 2 + (size.width / 3) * kotlin.math.cos(Math.toRadians(progress.toDouble())).toFloat()
            val y = size.height / 2 + (size.height / 3) * kotlin.math.sin(Math.toRadians(progress.toDouble())).toFloat()

            drawCircle(
                color = particleColor,
                radius = 2f,
                center = Offset(x, y)
            )
        }
    }
}

