package com.exemple.facilita.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.exemple.facilita.model.ServicoDetalhe
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exemple.facilita.viewmodel.ServicoViewModel
import com.exemple.facilita.viewmodel.FinalizarServicoState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.exemple.facilita.components.SwipeToFinishButton
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesServicoAceito(
    navController: NavController,
    servicoDetalhe: ServicoDetalhe,
    servicoViewModel: ServicoViewModel = viewModel()
) {
    val context = LocalContext.current
    val finalizarState by servicoViewModel.finalizarServicoState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Observar estado de finalização
    LaunchedEffect(finalizarState) {
        when (finalizarState) {
            is FinalizarServicoState.Success -> {
                showSuccessDialog = true
            }
            is FinalizarServicoState.Error -> {
                errorMessage = (finalizarState as FinalizarServicoState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // Cores inspiradas no iFood com identidade do projeto
    val primaryGreen = Color(0xFF00B14F)
    val bgLight = Color(0xFFFAFAFA)
    val cardBg = Color.White
    val textPrimary = Color(0xFF1A1A1A)
    val textSecondary = Color(0xFF757575)
    val dividerColor = Color(0xFFEEEEEE)
    val lightGreenBg = Color(0xFFE8F5E9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalhes do Serviço",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
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
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            // Botão de finalizar fixo na parte inferior
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Finalizar serviço",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Deslize o botão abaixo para confirmar a finalização",
                        fontSize = 13.sp,
                        color = textSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SwipeToFinishButton(
                        text = "Deslize para finalizar",
                        isEnabled = finalizarState !is FinalizarServicoState.Loading,
                        isLoading = finalizarState is FinalizarServicoState.Loading,
                        backgroundColor = primaryGreen,
                        onSwipeComplete = {
                            servicoViewModel.finalizarServico(
                                servicoId = servicoDetalhe.id,
                                context = context,
                                valorServico = servicoDetalhe.valor.toDoubleOrNull()
                            )
                        }
                    )
                }
            }
        },
        containerColor = bgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Card Cliente - Estilo iFood
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar com inicial
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(lightGreenBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = servicoDetalhe.contratante.usuario.nome
                                    .split(" ")
                                    .firstOrNull()
                                    ?.firstOrNull()
                                    ?.uppercase() ?: "C",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info Cliente
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = servicoDetalhe.contratante.usuario.nome,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = servicoDetalhe.contratante.usuario.telefone,
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Botões de Ação - Estilo iFood
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botão Ligar
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${servicoDetalhe.contratante.usuario.telefone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryGreen
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp,
                                brush = androidx.compose.ui.graphics.SolidColor(primaryGreen)
                            )
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ligar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Botão Chat
                        // Botão Chat
                        Button(
                            onClick = {
                                val servicoId = servicoDetalhe.id
                                val contratanteId = servicoDetalhe.contratante.id
                                val contratanteNome = java.net.URLEncoder.encode(servicoDetalhe.contratante.usuario.nome, "UTF-8")

                                // Buscar ID e nome do prestador LOGADO (não do serviço)
                                val prestadorId = com.exemple.facilita.utils.TokenManager.obterUsuarioId(context) ?: 0
                                val prestadorNome = java.net.URLEncoder.encode(
                                    com.exemple.facilita.utils.TokenManager.obterNomeUsuario(context) ?: "Prestador",
                                    "UTF-8"
                                )

                                android.util.Log.d("TelaDetalhes", "🔗 Navegando para chat: servicoId=$servicoId, contratanteId=$contratanteId, prestadorId=$prestadorId")

                                navController.navigate(
                                    "chat_ao_vivo/$servicoId/$contratanteId/$contratanteNome/$prestadorId/$prestadorNome"
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Message,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Chat",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Botão Localização
                        Button(
                            onClick = {
                                val servicoId = servicoDetalhe.id
                                val contratanteNome = java.net.URLEncoder.encode(servicoDetalhe.contratante.usuario.nome, "UTF-8")

                                android.util.Log.d("TelaDetalhes", "📍 Navegando para localização: servicoId=$servicoId")

                                navController.navigate(
                                    "acompanhamento_localizacao/$servicoId/$contratanteNome"
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Mapa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Card Detalhes do Serviço
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informações do serviço",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(lightGreenBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Categoria",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Text(
                                text = servicoDetalhe.categoria.nome,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Valor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valor do serviço",
                            fontSize = 15.sp,
                            color = textSecondary
                        )
                        Text(
                            text = "R$ ${servicoDetalhe.valor}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Descrição
                    Text(
                        text = "Descrição",
                        fontSize = 12.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = servicoDetalhe.descricao,
                        fontSize = 15.sp,
                        color = textPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            // Card Rota / Localização - Estilo iFood
            servicoDetalhe.localizacao?.let { loc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = primaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local do serviço",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Endereço formatado
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = loc.endereco + if (!loc.numero.isNullOrBlank()) ", ${loc.numero}" else "",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                            if (!loc.complemento.isNullOrBlank()) {
                                Text(
                                    text = loc.complemento,
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${loc.bairro} - ${loc.cidade}, ${loc.estado}",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                            Text(
                                text = "CEP: ${loc.cep}",
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botão de navegação moderno
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "google.navigation:q=${loc.latitude},${loc.longitude}&mode=d"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val browserIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${loc.latitude},${loc.longitude}")
                                    )
                                    context.startActivity(browserIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Iniciar rota no Google Maps",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Espaço extra no final para não ficar escondido atrás do bottomBar
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // Diálogo de sucesso com animação
    if (showSuccessDialog) {
        SuccessAnimationDialog(
            primaryGreen = primaryGreen,
            onDismiss = {
                showSuccessDialog = false
                servicoViewModel.resetFinalizarState()
                navController.navigate("tela_inicio_prestador") {
                    popUpTo("tela_inicio_prestador") { inclusive = true }
                }
            }
        )
    }

    // Diálogo de erro
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                servicoViewModel.resetFinalizarState()
            },
            title = {
                Text(
                    text = "❌ Erro",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                        servicoViewModel.resetFinalizarState()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SuccessAnimationDialog(
    primaryGreen: Color,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Anima entrada
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) { value, _ ->
            scale = value
            alpha = value
        }

        // Aguarda e fecha
        kotlinx.coroutines.delay(2500)
        onDismiss()
    }

    // Animação de pulse para o círculo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Partículas de fundo
        repeat(30) { index ->
            val randomX = remember { Random.nextFloat() }
            val randomY = remember { Random.nextFloat() }
            val randomSize = remember { Random.nextInt(2, 8) }
            val delay = remember { Random.nextInt(0, 2000) }

            var particleAlpha by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(delay.toLong())
                androidx.compose.animation.core.animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(1500)
                ) { value, _ ->
                    particleAlpha = value
                }
            }

            Box(
                modifier = Modifier
                    .offset(
                        x = (randomX * 350).dp - 175.dp,
                        y = (randomY * 600).dp - 300.dp
                    )
                    .size(randomSize.dp)
                    .alpha(particleAlpha * 0.6f)
                    .background(
                        primaryGreen.copy(alpha = 0.8f),
                        CircleShape
                    )
            )
        }

        // Card principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .alpha(alpha),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF8F9FA)
                            )
                        )
                    )
                    .padding(40.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Círculos concêntricos com gradiente
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo externo com pulse
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        primaryGreen.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Círculo do meio rotativo
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .rotate(rotation)
                            .background(
                                androidx.compose.ui.graphics.Brush.sweepGradient(
                                    colors = listOf(
                                        primaryGreen.copy(alpha = 0.3f),
                                        primaryGreen.copy(alpha = 0.1f),
                                        primaryGreen.copy(alpha = 0.3f)
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Círculo principal com gradiente
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        primaryGreen,
                                        Color(0xFF00D563)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(50.dp)
                                .scale(scale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Título moderno
                Text(
                    text = "Serviço Finalizado",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Linha decorativa com gradiente
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryGreen,
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "O serviço foi concluído com sucesso",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Redirecionando...",
                    fontSize = 14.sp,
                    color = primaryGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Anéis orbitais decorativos
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((200 + index * 50).dp)
                    .alpha(0.1f * (3 - index))
                    .rotate(rotation * (1 + index * 0.3f))
                    .border(
                        width = 2.dp,
                        color = primaryGreen.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

