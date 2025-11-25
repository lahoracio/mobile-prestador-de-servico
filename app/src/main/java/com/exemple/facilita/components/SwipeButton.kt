package com.exemple.facilita.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToFinishButton(
    text: String = "Deslize para finalizar",
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    backgroundColor: Color = Color(0xFF00B14F),
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }
    var containerWidth by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Largura disponível para deslizar (largura do container menos o thumb)
    val maxWidth = remember(containerWidth) {
        if (containerWidth > 0) (containerWidth - 200).toFloat() else 0f // 200 = tamanho aproximado do thumb + padding
    }

    val animatedOffset = remember { Animatable(0f) }

    LaunchedEffect(isCompleted) {
        if (isCompleted && maxWidth > 0) {
            animatedOffset.animateTo(
                targetValue = maxWidth,
                animationSpec = tween(300)
            )
            kotlinx.coroutines.delay(200)
            onSwipeComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = if (isEnabled) backgroundColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(32.dp)
            )
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            }
    ) {
        // Texto de instrução
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 70.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (isLoading) "Finalizando..." else text,
                color = if (isEnabled) backgroundColor else Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alpha(1f - (offsetX / maxWidth).coerceIn(0f, 1f))
            )
        }

        // Ícone de seta no final
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (isEnabled) backgroundColor else Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(0.5f)
            )
        }

        // Thumb deslizante
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(4.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(isEnabled, isLoading) {
                    if (isEnabled && !isLoading) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX >= maxWidth * 0.85f) {
                                        // Completou o deslize
                                        isCompleted = true
                                        offsetX = maxWidth
                                    } else {
                                        // Volta para o início
                                        animatedOffset.animateTo(0f, animationSpec = tween(200))
                                        offsetX = 0f
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    animatedOffset.animateTo(0f, animationSpec = tween(200))
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newOffset = (offsetX + dragAmount).coerceIn(0f, maxWidth)
                                offsetX = newOffset
                                scope.launch {
                                    animatedOffset.snapTo(newOffset)
                                }
                            }
                        )
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (isEnabled) backgroundColor else Color.Gray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = if (isCompleted) "Concluído" else "Deslize",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

