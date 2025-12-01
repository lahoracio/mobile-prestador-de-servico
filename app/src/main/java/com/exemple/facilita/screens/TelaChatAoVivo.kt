package com.exemple.facilita.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exemple.facilita.model.ChatMessage
import com.exemple.facilita.viewmodel.ChatViewModel
import com.exemple.facilita.websocket.ChatSocketManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChatAoVivo(
    navController: NavController,
    servicoId: Int,
    contratanteId: Int,
    contratanteNome: String,
    prestadorId: Int,
    prestadorNome: String,
    chatViewModel: ChatViewModel = viewModel()
) {
    // Cores do tema moderno
    val primaryGreen = Color(0xFF2E7D32)
    val accentCyan = Color(0xFF00FF88)
    val lightBg = Color(0xFFF5F5F5)
    val cardBg = Color.White
    val textPrimary = Color(0xFF212121)
    val textSecondary = Color(0xFF757575)
    val myMessageBg = Color(0xFF2E7D32)
    val theirMessageBg = Color(0xFFE8F5E9)

    // Estados
    var messageText by remember { mutableStateOf("") }
    val messages by chatViewModel.messages.collectAsState()
    val connectionState by chatViewModel.connectionState.collectAsState()
    val typingIndicator by chatViewModel.typingIndicator.collectAsState()
    val errorMessage by chatViewModel.errorMessage.collectAsState()

    // Lista para scroll automático
    val listState = rememberLazyListState()

    // Animações
    var isVisible by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Inicializa o chat ao carregar
    LaunchedEffect(servicoId) {
        isVisible = true
        chatViewModel.initializeChat(
            servicoId = servicoId,
            userId = prestadorId,
            userName = prestadorNome,
            userType = "prestador"
        )
    }

    // Auto-scroll quando novas mensagens chegam
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Cleanup ao sair
    DisposableEffect(servicoId) {
        onDispose {
            chatViewModel.leaveChat(servicoId)
        }
    }

    // Mostra erro se houver
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Log.e("TelaChatAoVivo", "Erro: $error")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        lightBg,
                        Color(0xFFEEEEEE)
                    )
                )
            )
    ) {
        // Efeitos de fundo decorativos
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(primaryGreen.copy(alpha = 0.1f), CircleShape)
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(accentCyan.copy(alpha = 0.08f), CircleShape)
                .blur(60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header personalizado
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão voltar
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    lightBg,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryGreen, accentCyan)
                                    )
                                )
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contratanteNome.firstOrNull()?.toString() ?: "C",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Nome e status
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = contratanteNome,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Indicador de conexão
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (connectionState) {
                                                ChatSocketManager.ConnectionState.CONNECTED -> Color(
                                                    0xFF4CAF50
                                                )
                                                ChatSocketManager.ConnectionState.CONNECTING -> Color(
                                                    0xFFFFC107
                                                )
                                                else -> Color(0xFFF44336)
                                            }
                                        )
                                        .alpha(pulseAlpha)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = when (connectionState) {
                                        ChatSocketManager.ConnectionState.CONNECTED -> "Online"
                                        ChatSocketManager.ConnectionState.CONNECTING -> "Conectando..."
                                        ChatSocketManager.ConnectionState.DISCONNECTED -> "Offline"
                                        ChatSocketManager.ConnectionState.ERROR -> "Erro"
                                    },
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        // Menu de opções
                        IconButton(
                            onClick = { /* Opções */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(lightBg, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opções",
                                tint = textPrimary
                            )
                        }
                    }
                }
            }

            // Área de mensagens
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        // Estado vazio
                        EmptyChatState()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages, key = { it.id }) { message ->
                                MessageBubble(
                                    message = message,
                                    isMyMessage = message.sender == "prestador",
                                    myMessageBg = myMessageBg,
                                    theirMessageBg = theirMessageBg,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }

                            // Indicador de digitação
                            if (typingIndicator.first) {
                                item {
                                    TypingIndicatorBubble(
                                        userName = typingIndicator.second,
                                        theirMessageBg = theirMessageBg
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Campo de entrada
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = cardBg,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Campo de texto
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { newText ->
                                messageText = newText
                                if (newText.isNotBlank()) {
                                    chatViewModel.startTypingIndicator(servicoId)
                                } else {
                                    chatViewModel.stopTypingIndicator(servicoId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp, max = 120.dp),
                            placeholder = {
                                Text(
                                    "Digite uma mensagem...",
                                    color = textSecondary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (messageText.isNotBlank()) {
                                        chatViewModel.sendMessage(
                                            servicoId = servicoId,
                                            mensagem = messageText,
                                            targetUserId = contratanteId
                                        )
                                        messageText = ""
                                    }
                                }
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Botão enviar
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    chatViewModel.sendMessage(
                                        servicoId = servicoId,
                                        mensagem = messageText,
                                        targetUserId = contratanteId
                                    )
                                    messageText = ""
                                }
                            },
                            containerColor = if (messageText.isNotBlank()) primaryGreen else Color(
                                0xFFE0E0E0
                            ),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Snackbar para erros
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LaunchedEffect(error) {
                delay(3000)
                chatViewModel.clearError()
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMyMessage: Boolean,
    myMessageBg: Color,
    theirMessageBg: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) myMessageBg else theirMessageBg
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMyMessage) 16.dp else 4.dp,
                bottomEnd = if (isMyMessage) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!isMyMessage) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = message.mensagem,
                    fontSize = 15.sp,
                    color = if (isMyMessage) Color.White else textPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = if (isMyMessage) Color.White.copy(alpha = 0.8f) else textSecondary
                    )

                    if (isMyMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Enviado",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    userName: String,
    theirMessageBg: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = theirMessageBg
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$userName está digitando",
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.width(8.dp))
                TypingDots()
            }
        }
    }
}

@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF757575).copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF2E7D32).copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nenhuma mensagem ainda",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Envie uma mensagem para iniciar\na conversa com o cliente",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

