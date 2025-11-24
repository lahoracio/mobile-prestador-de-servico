package com.exemple.facilita.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ChatMessage
import com.exemple.facilita.websocket.ChatSocketManager
import kotlinx.coroutines.launch
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
    prestadorNome: String
) {
    // Log de debug dos parâmetros recebidos
    android.util.Log.d("TelaChatAoVivo", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    android.util.Log.d("TelaChatAoVivo", "📱 TELA CHAT INICIADA")
    android.util.Log.d("TelaChatAoVivo", "🔢 servicoId: $servicoId")
    android.util.Log.d("TelaChatAoVivo", "👤 contratanteId: $contratanteId")
    android.util.Log.d("TelaChatAoVivo", "📝 contratanteNome: $contratanteNome")
    android.util.Log.d("TelaChatAoVivo", "👨‍💼 prestadorId: $prestadorId")
    android.util.Log.d("TelaChatAoVivo", "📝 prestadorNome: $prestadorNome")
    android.util.Log.d("TelaChatAoVivo", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cores
    val primaryGreen = Color(0xFF00B14F)
    val bgLight = Color(0xFFFAFAFA)
    val myMessageBg = Color(0xFFE8F5E9)
    val otherMessageBg = Color.White
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF757575)

    // Estados
    var messageText by remember { mutableStateOf("") }
    val chatRepository = remember { com.exemple.facilita.data.ChatRepository(context) }
    val messages = remember { mutableStateListOf<ChatMessage>().apply { addAll(chatRepository.loadMessages(servicoId)) } }
    var isConnected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Gerenciador do Socket (Singleton - mantém conexão entre telas)
    val chatManager = remember {
        android.util.Log.d("TelaChatAoVivo", "🔧 Obtendo instância do ChatSocketManager...")
        android.util.Log.d("TelaChatAoVivo", "   userId: $prestadorId")
        android.util.Log.d("TelaChatAoVivo", "   userType: prestador")
        android.util.Log.d("TelaChatAoVivo", "   userName: $prestadorNome")

        ChatSocketManager.getInstance()
    }

    // Conectar ao WebSocket
    LaunchedEffect(Unit) {
        android.util.Log.d("TelaChatAoVivo", "🚀 Iniciando conexão WebSocket...")
        android.util.Log.d("TelaChatAoVivo", "   ServicoId para conectar: $servicoId")

        // TESTE: Verificar se Socket.IO funciona
        try {
            com.exemple.facilita.test.SocketIOTester.testarConexao()
        } catch (e: Exception) {
            android.util.Log.e("TelaChatAoVivo", "Erro ao testar socket: ${e.message}")
        }

        chatManager.connect(
            userId = prestadorId,
            userType = "prestador",
            userName = prestadorNome,
            servicoId = servicoId,
            onMessageReceived = { message ->
                android.util.Log.d("TelaChatAoVivo", "📩 Mensagem recebida no UI: ${message.mensagem}")

                // Adicionar à lista e salvar localmente (garantir main thread)
                scope.launch {
                    messages.add(message)
                    chatRepository.saveMessages(servicoId, messages.toList())

                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            },
            onError = { error ->
                android.util.Log.e("TelaChatAoVivo", "❌ Erro recebido no UI: $error")
                errorMessage = error
            }
        )

        // Aguardar um pouco para verificar conexão
        android.util.Log.d("TelaChatAoVivo", "⏳ Aguardando 1s para verificar conexão...")
        kotlinx.coroutines.delay(1000)
        isConnected = chatManager.isConnected()
        android.util.Log.d("TelaChatAoVivo", "✅ Status de conexão: $isConnected")

        // Verificar status de conexão periodicamente
        while (true) {
            kotlinx.coroutines.delay(2000)
            val connected = chatManager.isConnected()
            if (connected != isConnected) {
                isConnected = connected
                android.util.Log.d("TelaChatAoVivo", "Status de conexão mudou: $connected")
            }
        }
    }

    // NÃO desconectar ao sair - mantém conexão ativa
    // A conexão é singleton e persiste entre navegações
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("TelaChatAoVivo", "📴 Saindo da tela (conexão mantida)")
            // Salvar mensagens ao sair
            chatRepository.saveMessages(servicoId, messages.toList())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = contratanteNome,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isConnected) primaryGreen else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isConnected) "Online" else "Offline",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Voltar",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = textPrimary
                )
            )
        },
        containerColor = bgLight,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Digite sua mensagem...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        maxLines = 4
                    )

                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                if (!isConnected) {
                                    errorMessage = "Você está offline. Aguarde a reconexão."
                                    return@FloatingActionButton
                                }

                                val mensagemParaEnviar = messageText.trim()

                                chatManager.sendMessage(
                                    servicoId = servicoId,
                                    mensagem = mensagemParaEnviar,
                                    targetUserId = contratanteId,
                                    onSuccess = {
                                        // Mensagem enviada com sucesso
                                        android.util.Log.d("TelaChatAoVivo", "✅ Mensagem enviada! Aguardando broadcast...")
                                    },
                                    onError = { error ->
                                        // Erro ao enviar
                                        android.util.Log.e("TelaChatAoVivo", "❌ Erro: $error")
                                        errorMessage = error
                                    }
                                )

                                // NÃO adicionar localmente - o broadcast do servidor já vai adicionar
                                // Isso evita mensagens duplicadas
                                messageText = ""
                            }
                        },
                        containerColor = if (messageText.isNotBlank() && isConnected) primaryGreen else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (messages.isEmpty()) {
                // Estado vazio
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma mensagem ainda",
                        fontSize = 16.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Envie a primeira mensagem para iniciar a conversa",
                        fontSize = 14.sp,
                        color = Color(0xFFBDBDBD),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Lista de mensagens
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isMyMessage = message.sender == "prestador",
                            myMessageBg = myMessageBg,
                            otherMessageBg = otherMessageBg,
                            primaryGreen = primaryGreen,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }
            }

            // Mensagem de erro
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFC62828),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMyMessage: Boolean,
    myMessageBg: Color,
    otherMessageBg: Color,
    primaryGreen: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMyMessage) 16.dp else 4.dp,
                bottomEnd = if (isMyMessage) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMyMessage) myMessageBg else otherMessageBg
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isMyMessage) {
                    Text(
                        text = message.userName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.mensagem,
                    fontSize = 15.sp,
                    color = textPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = textSecondary,
                    modifier = Modifier.align(if (isMyMessage) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: String): String {
    return try {
        // Input format: timestamp vem em UTC (ISO 8601)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }

        // Output format: horário local do dispositivo
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: "Agora"
    } catch (e: Exception) {
        android.util.Log.e("TelaChatAoVivo", "Erro ao formatar timestamp: ${e.message}")
        "Agora"
    }
}

